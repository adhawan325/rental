#!/bin/bash

# ============================================
# Deployment Script for Jmix Rental Application
# ============================================
# This script helps deploy the application to AWS using CloudFormation

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if AWS CLI is installed
check_aws_cli() {
    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI is not installed. Please install it first."
        exit 1
    fi
    print_info "AWS CLI found: $(aws --version)"
}

# Function to check if required parameters are set
check_parameters() {
    if [ -z "$AWS_REGION" ]; then
        print_error "AWS_REGION environment variable is not set"
        exit 1
    fi
    
    if [ -z "$AWS_ACCOUNT_ID" ]; then
        AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
        print_info "AWS Account ID: $AWS_ACCOUNT_ID"
    fi
}

# Function to create ECR repository
create_ecr_repository() {
    print_info "Creating ECR repository..."
    
    # Get the directory where this script is located
    SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
    
    aws cloudformation deploy \
        --template-file "$SCRIPT_DIR/ecr-repository.yaml" \
        --stack-name rental-ecr \
        --region $AWS_REGION \
        --no-fail-on-empty-changeset
    
    ECR_URI=$(aws cloudformation describe-stacks \
        --stack-name rental-ecr \
        --region $AWS_REGION \
        --query 'Stacks[0].Outputs[?OutputKey==`RepositoryUri`].OutputValue' \
        --output text)
    
    print_info "ECR Repository URI: $ECR_URI"
}

# Function to build and push Docker image
build_and_push_image() {
    print_info "Building Docker image..."
    
    # Navigate to project root
    cd "$(dirname "$0")/.."
    
    # Build the image
    docker build -t rental:latest .
    
    # Tag the image
    docker tag rental:latest $ECR_URI:latest
    docker tag rental:latest $ECR_URI:$(date +%Y%m%d-%H%M%S)
    
    # Login to ECR
    print_info "Logging in to ECR..."
    aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_URI
    
    # Push the image
    print_info "Pushing Docker image to ECR..."
    docker push $ECR_URI:latest
    docker push $ECR_URI:$(date +%Y%m%d-%H%M%S)
    
    print_info "Docker image pushed successfully"
}

# Function to deploy lower environment
deploy_lower_env() {
    local ENV_NAME=$1
    
    print_info "Deploying $ENV_NAME environment..."
    
    # Check if parameters file exists
    if [ ! -f "cloudformation/parameters-${ENV_NAME}.json" ]; then
        print_error "Parameters file not found: cloudformation/parameters-${ENV_NAME}.json"
        print_info "Please create it using parameters-template.json as a reference"
        exit 1
    fi
    
    aws cloudformation deploy \
        --template-file cloudformation/lower-env-stack.yaml \
        --stack-name rental-${ENV_NAME} \
        --parameter-overrides file://cloudformation/parameters-${ENV_NAME}.json \
        --capabilities CAPABILITY_NAMED_IAM \
        --region $AWS_REGION \
        --no-fail-on-empty-changeset
    
    # Get outputs
    print_info "Deployment complete. Getting outputs..."
    aws cloudformation describe-stacks \
        --stack-name rental-${ENV_NAME} \
        --region $AWS_REGION \
        --query 'Stacks[0].Outputs' \
        --output table
}

# Function to deploy production environment
deploy_production() {
    print_info "Deploying production environment..."
    
    # Check if parameters file exists
    if [ ! -f "cloudformation/parameters-prod.json" ]; then
        print_error "Parameters file not found: cloudformation/parameters-prod.json"
        print_info "Please create it using parameters-template.json as a reference"
        exit 1
    fi
    
    # Confirm production deployment
    print_warn "You are about to deploy to PRODUCTION. This will create:"
    print_warn "  - Auto Scaling Group with EC2 instances"
    print_warn "  - RDS PostgreSQL database (Multi-AZ)"
    print_warn "  - Application Load Balancer"
    print_warn "  - EFS file system"
    print_warn "  - NAT Gateways (additional cost)"
    echo ""
    read -p "Are you sure you want to continue? (yes/no): " confirm
    
    if [ "$confirm" != "yes" ]; then
        print_info "Deployment cancelled"
        exit 0
    fi
    
    aws cloudformation deploy \
        --template-file cloudformation/production-stack.yaml \
        --stack-name rental-production \
        --parameter-overrides file://cloudformation/parameters-prod.json \
        --capabilities CAPABILITY_NAMED_IAM \
        --region $AWS_REGION \
        --no-fail-on-empty-changeset
    
    # Get outputs
    print_info "Deployment complete. Getting outputs..."
    aws cloudformation describe-stacks \
        --stack-name rental-production \
        --region $AWS_REGION \
        --query 'Stacks[0].Outputs' \
        --output table
}

# Function to update application (rolling update)
update_application() {
    local ENV_NAME=$1
    
    print_info "Updating application in $ENV_NAME environment..."
    
    # Build and push new image
    build_and_push_image
    
    if [ "$ENV_NAME" == "production" ]; then
        # For production, trigger Auto Scaling Group instance refresh
        ASG_NAME=$(aws cloudformation describe-stacks \
            --stack-name rental-production \
            --region $AWS_REGION \
            --query 'Stacks[0].Outputs[?OutputKey==`AutoScalingGroupName`].OutputValue' \
            --output text)
        
        print_info "Starting instance refresh for Auto Scaling Group: $ASG_NAME"
        aws autoscaling start-instance-refresh \
            --auto-scaling-group-name $ASG_NAME \
            --region $AWS_REGION \
            --preferences '{"MinHealthyPercentage": 50, "InstanceWarmup": 300}'
        
        print_info "Instance refresh started. Monitor progress in AWS Console."
    else
        # For lower environments, restart the instance
        INSTANCE_ID=$(aws cloudformation describe-stacks \
            --stack-name rental-${ENV_NAME} \
            --region $AWS_REGION \
            --query 'Stacks[0].Outputs[?OutputKey==`InstanceId`].OutputValue' \
            --output text)
        
        print_info "Restarting instance: $INSTANCE_ID"
        aws ec2 reboot-instances --instance-ids $INSTANCE_ID --region $AWS_REGION
        
        print_info "Instance restarted. Application will pull the latest image on startup."
    fi
}

# Function to show help
show_help() {
    cat << EOF
Usage: ./deploy.sh [COMMAND] [OPTIONS]

Commands:
    setup               Create ECR repository
    build               Build and push Docker image
    deploy-dev          Deploy development environment
    deploy-test         Deploy test environment
    deploy-staging      Deploy staging environment
    deploy-prod         Deploy production environment
    update-dev          Update application in dev environment
    update-test         Update application in test environment
    update-staging      Update application in staging environment
    update-prod         Update application in production environment
    help                Show this help message

Environment Variables:
    AWS_REGION          AWS region (required)
    AWS_ACCOUNT_ID      AWS account ID (optional, will be auto-detected)

Examples:
    # Setup ECR repository
    AWS_REGION=us-east-1 ./deploy.sh setup

    # Build and push Docker image
    AWS_REGION=us-east-1 ./deploy.sh build

    # Deploy to dev environment
    AWS_REGION=us-east-1 ./deploy.sh deploy-dev

    # Update production application
    AWS_REGION=us-east-1 ./deploy.sh update-prod

EOF
}

# Main script
main() {
    check_aws_cli
    
    case "$1" in
        setup)
            check_parameters
            create_ecr_repository
            ;;
        build)
            check_parameters
            create_ecr_repository
            build_and_push_image
            ;;
        deploy-dev)
            check_parameters
            deploy_lower_env "dev"
            ;;
        deploy-test)
            check_parameters
            deploy_lower_env "test"
            ;;
        deploy-staging)
            check_parameters
            deploy_lower_env "staging"
            ;;
        deploy-prod)
            check_parameters
            deploy_production
            ;;
        update-dev)
            check_parameters
            update_application "dev"
            ;;
        update-test)
            check_parameters
            update_application "test"
            ;;
        update-staging)
            check_parameters
            update_application "staging"
            ;;
        update-prod)
            check_parameters
            update_application "production"
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Unknown command: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# Run main function
main "$@"