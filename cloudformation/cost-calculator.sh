#!/bin/bash

# ============================================
# AWS Cost Calculator for Rental Application
# ============================================
# Estimates monthly costs for different deployment scenarios

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Pricing (US East 1, as of 2024 - update as needed)
EC2_T3_SMALL_HOURLY=0.0208
EC2_T3_MEDIUM_HOURLY=0.0416
EC2_T3_LARGE_HOURLY=0.0832
EC2_T3_XLARGE_HOURLY=0.1664

SPOT_DISCOUNT=0.70  # 70% discount on average

RDS_T3_SMALL_HOURLY=0.034
RDS_T3_MEDIUM_HOURLY=0.068
RDS_T3_LARGE_HOURLY=0.136
RDS_MULTI_AZ_MULTIPLIER=2

EBS_GP3_PER_GB=0.08
EFS_STANDARD_PER_GB=0.30
EFS_IA_PER_GB=0.025

S3_STANDARD_PER_GB=0.023
S3_GLACIER_PER_GB=0.004

ALB_HOURLY=0.0225
ALB_LCU_HOURLY=0.008

NAT_GATEWAY_HOURLY=0.045
NAT_GATEWAY_DATA_GB=0.045

DATA_TRANSFER_OUT_GB=0.09

CLOUDWATCH_LOGS_GB=0.50
CLOUDWATCH_METRICS=0.30

HOURS_PER_MONTH=730

# Functions
calculate_ec2_cost() {
    local instance_type=$1
    local count=$2
    local use_spot=$3
    
    local hourly_rate
    case $instance_type in
        "t3.small")
            hourly_rate=$EC2_T3_SMALL_HOURLY
            ;;
        "t3.medium")
            hourly_rate=$EC2_T3_MEDIUM_HOURLY
            ;;
        "t3.large")
            hourly_rate=$EC2_T3_LARGE_HOURLY
            ;;
        "t3.xlarge")
            hourly_rate=$EC2_T3_XLARGE_HOURLY
            ;;
        *)
            hourly_rate=$EC2_T3_MEDIUM_HOURLY
            ;;
    esac
    
    if [ "$use_spot" == "true" ]; then
        hourly_rate=$(echo "$hourly_rate * $SPOT_DISCOUNT" | bc -l)
    fi
    
    echo "$(echo "$hourly_rate * $count * $HOURS_PER_MONTH" | bc -l)"
}

calculate_rds_cost() {
    local instance_class=$1
    local multi_az=$2
    
    local hourly_rate
    case $instance_class in
        "db.t3.small")
            hourly_rate=$RDS_T3_SMALL_HOURLY
            ;;
        "db.t3.medium")
            hourly_rate=$RDS_T3_MEDIUM_HOURLY
            ;;
        "db.t3.large")
            hourly_rate=$RDS_T3_LARGE_HOURLY
            ;;
        *)
            hourly_rate=$RDS_T3_MEDIUM_HOURLY
            ;;
    esac
    
    if [ "$multi_az" == "true" ]; then
        hourly_rate=$(echo "$hourly_rate * $RDS_MULTI_AZ_MULTIPLIER" | bc -l)
    fi
    
    echo "$(echo "$hourly_rate * $HOURS_PER_MONTH" | bc -l)"
}

print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""
}

print_cost_line() {
    local service=$1
    local cost=$2
    printf "%-30s ${GREEN}\$%.2f${NC}\n" "$service:" "$cost"
}

# Development Environment Cost
calculate_dev_cost() {
    print_header "Development Environment (Spot Instance)"
    
    local ec2_cost=$(calculate_ec2_cost "t3.medium" 1 "true")
    local ebs_cost=$(echo "$EBS_GP3_PER_GB * 50" | bc -l)
    local s3_cost=$(echo "$S3_STANDARD_PER_GB * 10" | bc -l)
    local data_transfer=$(echo "$DATA_TRANSFER_OUT_GB * 50" | bc -l)
    local cloudwatch=$(echo "$CLOUDWATCH_LOGS_GB * 5 + $CLOUDWATCH_METRICS * 10" | bc -l)
    
    print_cost_line "EC2 t3.medium (Spot)" "$ec2_cost"
    print_cost_line "EBS 50GB gp3" "$ebs_cost"
    print_cost_line "S3 Backups (10GB)" "$s3_cost"
    print_cost_line "Data Transfer (50GB)" "$data_transfer"
    print_cost_line "CloudWatch" "$cloudwatch"
    
    local total=$(echo "$ec2_cost + $ebs_cost + $s3_cost + $data_transfer + $cloudwatch" | bc -l)
    echo ""
    print_cost_line "TOTAL (Monthly)" "$total"
    echo ""
}

# Development Environment Cost (On-Demand)
calculate_dev_ondemand_cost() {
    print_header "Development Environment (On-Demand)"
    
    local ec2_cost=$(calculate_ec2_cost "t3.medium" 1 "false")
    local ebs_cost=$(echo "$EBS_GP3_PER_GB * 50" | bc -l)
    local s3_cost=$(echo "$S3_STANDARD_PER_GB * 10" | bc -l)
    local data_transfer=$(echo "$DATA_TRANSFER_OUT_GB * 50" | bc -l)
    local cloudwatch=$(echo "$CLOUDWATCH_LOGS_GB * 5 + $CLOUDWATCH_METRICS * 10" | bc -l)
    
    print_cost_line "EC2 t3.medium (On-Demand)" "$ec2_cost"
    print_cost_line "EBS 50GB gp3" "$ebs_cost"
    print_cost_line "S3 Backups (10GB)" "$s3_cost"
    print_cost_line "Data Transfer (50GB)" "$data_transfer"
    print_cost_line "CloudWatch" "$cloudwatch"
    
    local total=$(echo "$ec2_cost + $ebs_cost + $s3_cost + $data_transfer + $cloudwatch" | bc -l)
    echo ""
    print_cost_line "TOTAL (Monthly)" "$total"
    echo ""
}

# Production Environment Cost (Base)
calculate_prod_base_cost() {
    print_header "Production Environment (Base - 2 Instances)"
    
    local ec2_cost=$(calculate_ec2_cost "t3.large" 2 "false")
    local rds_cost=$(calculate_rds_cost "db.t3.medium" "true")
    local ebs_cost=$(echo "$EBS_GP3_PER_GB * 60" | bc -l)  # 30GB per instance
    local rds_storage=$(echo "$EBS_GP3_PER_GB * 100 * 2" | bc -l)  # Multi-AZ doubles storage
    local efs_cost=$(echo "$EFS_STANDARD_PER_GB * 20" | bc -l)
    local s3_cost=$(echo "$S3_STANDARD_PER_GB * 20" | bc -l)
    local alb_cost=$(echo "($ALB_HOURLY + $ALB_LCU_HOURLY * 2) * $HOURS_PER_MONTH" | bc -l)
    local nat_cost=$(echo "($NAT_GATEWAY_HOURLY * 2 * $HOURS_PER_MONTH) + ($NAT_GATEWAY_DATA_GB * 100)" | bc -l)
    local data_transfer=$(echo "$DATA_TRANSFER_OUT_GB * 200" | bc -l)
    local cloudwatch=$(echo "$CLOUDWATCH_LOGS_GB * 20 + $CLOUDWATCH_METRICS * 50" | bc -l)
    
    print_cost_line "EC2 t3.large x2" "$ec2_cost"
    print_cost_line "RDS db.t3.medium (Multi-AZ)" "$rds_cost"
    print_cost_line "EBS (EC2 instances)" "$ebs_cost"
    print_cost_line "RDS Storage 100GB (Multi-AZ)" "$rds_storage"
    print_cost_line "EFS 20GB" "$efs_cost"
    print_cost_line "S3 Backups (20GB)" "$s3_cost"
    print_cost_line "Application Load Balancer" "$alb_cost"
    print_cost_line "NAT Gateways x2" "$nat_cost"
    print_cost_line "Data Transfer (200GB)" "$data_transfer"
    print_cost_line "CloudWatch" "$cloudwatch"
    
    local total=$(echo "$ec2_cost + $rds_cost + $ebs_cost + $rds_storage + $efs_cost + $s3_cost + $alb_cost + $nat_cost + $data_transfer + $cloudwatch" | bc -l)
    echo ""
    print_cost_line "TOTAL (Monthly)" "$total"
    echo ""
}

# Production Environment Cost (Peak)
calculate_prod_peak_cost() {
    print_header "Production Environment (Peak - 6 Instances)"
    
    local ec2_cost=$(calculate_ec2_cost "t3.large" 6 "false")
    local rds_cost=$(calculate_rds_cost "db.t3.medium" "true")
    local ebs_cost=$(echo "$EBS_GP3_PER_GB * 180" | bc -l)  # 30GB per instance
    local rds_storage=$(echo "$EBS_GP3_PER_GB * 100 * 2" | bc -l)
    local efs_cost=$(echo "$EFS_STANDARD_PER_GB * 50" | bc -l)
    local s3_cost=$(echo "$S3_STANDARD_PER_GB * 30" | bc -l)
    local alb_cost=$(echo "($ALB_HOURLY + $ALB_LCU_HOURLY * 5) * $HOURS_PER_MONTH" | bc -l)
    local nat_cost=$(echo "($NAT_GATEWAY_HOURLY * 2 * $HOURS_PER_MONTH) + ($NAT_GATEWAY_DATA_GB * 300)" | bc -l)
    local data_transfer=$(echo "$DATA_TRANSFER_OUT_GB * 500" | bc -l)
    local cloudwatch=$(echo "$CLOUDWATCH_LOGS_GB * 50 + $CLOUDWATCH_METRICS * 100" | bc -l)
    
    print_cost_line "EC2 t3.large x6" "$ec2_cost"
    print_cost_line "RDS db.t3.medium (Multi-AZ)" "$rds_cost"
    print_cost_line "EBS (EC2 instances)" "$ebs_cost"
    print_cost_line "RDS Storage 100GB (Multi-AZ)" "$rds_storage"
    print_cost_line "EFS 50GB" "$efs_cost"
    print_cost_line "S3 Backups (30GB)" "$s3_cost"
    print_cost_line "Application Load Balancer" "$alb_cost"
    print_cost_line "NAT Gateways x2" "$nat_cost"
    print_cost_line "Data Transfer (500GB)" "$data_transfer"
    print_cost_line "CloudWatch" "$cloudwatch"
    
    local total=$(echo "$ec2_cost + $rds_cost + $ebs_cost + $rds_storage + $efs_cost + $s3_cost + $alb_cost + $nat_cost + $data_transfer + $cloudwatch" | bc -l)
    echo ""
    print_cost_line "TOTAL (Monthly)" "$total"
    echo ""
}

# Cost Optimization Recommendations
show_optimization_tips() {
    print_header "Cost Optimization Recommendations"
    
    echo -e "${YELLOW}1. Reserved Instances (Production)${NC}"
    echo "   - Save 30-40% on EC2 and RDS costs"
    echo "   - 1-year commitment: ~30% savings"
    echo "   - 3-year commitment: ~40% savings"
    echo ""
    
    echo -e "${YELLOW}2. Savings Plans${NC}"
    echo "   - Flexible alternative to Reserved Instances"
    echo "   - Commit to consistent usage ($/hour)"
    echo "   - Applies to EC2, Lambda, Fargate"
    echo ""
    
    echo -e "${YELLOW}3. EFS Lifecycle Management${NC}"
    echo "   - Move infrequent files to IA storage"
    echo "   - Save up to 92% on storage costs"
    echo "   - Automatic based on access patterns"
    echo ""
    
    echo -e "${YELLOW}4. S3 Lifecycle Policies${NC}"
    echo "   - Transition old backups to Glacier"
    echo "   - Save up to 83% on backup storage"
    echo "   - Already configured in templates"
    echo ""
    
    echo -e "${YELLOW}5. Schedule Dev/Test Environments${NC}"
    echo "   - Stop instances during off-hours"
    echo "   - Save 50-70% on dev/test costs"
    echo "   - Use AWS Instance Scheduler"
    echo ""
    
    echo -e "${YELLOW}6. Right-Sizing${NC}"
    echo "   - Monitor actual resource usage"
    echo "   - Downsize over-provisioned instances"
    echo "   - Use AWS Compute Optimizer"
    echo ""
    
    echo -e "${YELLOW}7. NAT Gateway Optimization${NC}"
    echo "   - Consider NAT instances for lower traffic"
    echo "   - Use VPC endpoints for AWS services"
    echo "   - Reduce data transfer costs"
    echo ""
}

# Main menu
show_menu() {
    echo ""
    echo -e "${BLUE}AWS Cost Calculator - Rental Application${NC}"
    echo ""
    echo "1. Development (Spot Instance)"
    echo "2. Development (On-Demand)"
    echo "3. Production (Base - 2 instances)"
    echo "4. Production (Peak - 6 instances)"
    echo "5. All Scenarios"
    echo "6. Cost Optimization Tips"
    echo "7. Exit"
    echo ""
    read -p "Select an option (1-7): " choice
    
    case $choice in
        1)
            calculate_dev_cost
            show_menu
            ;;
        2)
            calculate_dev_ondemand_cost
            show_menu
            ;;
        3)
            calculate_prod_base_cost
            show_menu
            ;;
        4)
            calculate_prod_peak_cost
            show_menu
            ;;
        5)
            calculate_dev_cost
            calculate_dev_ondemand_cost
            calculate_prod_base_cost
            calculate_prod_peak_cost
            show_menu
            ;;
        6)
            show_optimization_tips
            show_menu
            ;;
        7)
            echo "Goodbye!"
            exit 0
            ;;
        *)
            echo "Invalid option"
            show_menu
            ;;
    esac
}

# Check if bc is installed
if ! command -v bc &> /dev/null; then
    echo "Error: 'bc' calculator is required but not installed."
    echo "Install it with: brew install bc (macOS) or apt-get install bc (Linux)"
    exit 1
fi

# Run the menu
show_menu