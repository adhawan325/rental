# 🏗️ User Portal - Architecture & Data Flow

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER                          │
│                         (Vaadin Flow UI)                            │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │ Registration │  │    Login     │  │   Profile    │            │
│  │     View     │  │     View     │  │     View     │            │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘            │
│         │                  │                  │                     │
│  ┌──────▼───────┐  ┌──────▼───────┐  ┌──────▼───────┐            │
│  │   Password   │  │   Session    │  │     2FA      │            │
│  │ Management   │  │  Management  │  │     View     │            │
│  │     View     │  │     View     │  │              │            │
│  └──────────────┘  └──────────────┘  └──────────────┘            │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                │ HTTP/HTTPS
                                │
┌─────────────────────────────────────────────────────────────────────┐
│                         CONTROLLER LAYER                            │
│                      (REST API Endpoints)                           │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  /api/auth/register          POST   - User registration            │
│  /api/auth/verify            GET    - Email verification           │
│  /api/auth/login             POST   - Username/password login      │
│  /api/auth/logout            POST   - Logout                       │
│  /api/auth/forgot-password   POST   - Request password reset       │
│  /api/auth/reset-password    POST   - Reset password               │
│  /api/profile                GET    - Get user profile             │
│  /api/profile                PUT    - Update profile               │
│  /api/profile/password       POST   - Change password              │
│  /api/profile/2fa            POST   - Enable/disable 2FA           │
│  /api/sessions               GET    - List active sessions         │
│  /api/sessions/{id}          DELETE - Revoke session               │
│  /login/oauth2/code/google   GET    - OAuth2 callback (Google)     │
│  /login/oauth2/code/github   GET    - OAuth2 callback (GitHub)     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                │
┌─────────────────────────────────────────────────────────────────────┐
│                          SERVICE LAYER                              │
│                       (Business Logic)                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │ Registration     │  │   Password       │  │   Email         │ │
│  │   Service        │  │   Service        │  │   Service       │ │
│  │                  │  │                  │  │                 │ │
│  │ - Register user  │  │ - Change pwd     │  │ - Send email    │ │
│  │ - Verify email   │  │ - Reset pwd      │  │ - Templates     │ │
│  │ - Validate       │  │ - Validate       │  │ - Queue         │ │
│  └──────────────────┘  └──────────────────┘  └─────────────────┘ │
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │ Authentication   │  │   Session        │  │   2FA           │ │
│  │   Service        │  │   Service        │  │   Service       │ │
│  │                  │  │                  │  │                 │ │
│  │ - Login          │  │ - Track sessions │  │ - Setup TOTP    │ │
│  │ - Logout         │  │ - Revoke         │  │ - Verify code   │ │
│  │ - OAuth2 mapping │  │ - History        │  │ - Backup codes  │ │
│  └──────────────────┘  └──────────────────┘  └─────────────────┘ │
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐                       │
│  │ User             │  │   Audit          │                       │
│  │   Service        │  │   Service        │                       │
│  │                  │  │                  │                       │
│  │ - CRUD           │  │ - Log events     │                       │
│  │ - Profile mgmt   │  │ - Track changes  │                       │
│  │ - Account status │  │ - Compliance     │                       │
│  └──────────────────┘  └──────────────────┘                       │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                │
┌─────────────────────────────────────────────────────────────────────┐
│                         SECURITY LAYER                              │
│                    (Spring Security + Jmix)                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │                    Security Filter Chain                      │ │
│  │                                                               │ │
│  │  1. CSRF Protection                                           │ │
│  │  2. Session Management                                        │ │
│  │  3. Authentication (Username/Password, OAuth2)                │ │
│  │  4. Authorization (Role-based)                                │ │
│  │  5. 2FA Verification                                          │ │
│  │  6. Account Lockout                                           │ │
│  │  7. Audit Logging                                             │ │
│  │                                                               │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │ Password         │  │   Token          │  │   Session       │ │
│  │   Encoder        │  │   Manager        │  │   Registry      │ │
│  │                  │  │                  │  │                 │ │
│  │ - BCrypt (12)    │  │ - Generate       │  │ - Track         │ │
│  │ - Validate       │  │ - Validate       │  │ - Invalidate    │ │
│  │                  │  │ - Expire         │  │ - Concurrent    │ │
│  └──────────────────┘  └──────────────────┘  └─────────────────┘ │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                │
┌─────────────────────────────────────────────────────────────────────┐
│                       PERSISTENCE LAYER                             │
│                    (JPA + EclipseLink)                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │ User             │  │   Password       │  │   Email         │ │
│  │   Repository     │  │   History        │  │   Verification  │ │
│  │                  │  │   Repository     │  │   Token Repo    │ │
│  └──────────────────┘  └──────────────────┘  └─────────────────┘ │
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │ User             │  │   Login          │  │   Two Factor    │ │
│  │   Session        │  │   History        │  │   Auth          │ │
│  │   Repository     │  │   Repository     │  │   Repository    │ │
│  └──────────────────┘  └──────────────────┘  └─────────────────┘ │
│                                                                     │
│  ┌──────────────────┐                                              │
│  │ Social           │                                              │
│  │   Login          │                                              │
│  │   Connection     │                                              │
│  │   Repository     │                                              │
│  └──────────────────┘                                              │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                │
┌─────────────────────────────────────────────────────────────────────┐
│                         DATABASE LAYER                              │
│                          (PostgreSQL)                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  Tables:                                                            │
│  - RENTAL_USER                                                      │
│  - PASSWORD_HISTORY                                                 │
│  - EMAIL_VERIFICATION_TOKEN                                         │
│  - USER_SESSION                                                     │
│  - LOGIN_HISTORY                                                    │
│  - TWO_FACTOR_AUTH                                                  │
│  - SOCIAL_LOGIN_CONNECTION                                          │
│  - USER_SUBSCRIPTION (existing)                                     │
│  - BILLING_HISTORY (existing)                                       │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                                │
                                │
┌─────────────────────────────────────────────────────────────────────┐
│                      EXTERNAL SERVICES                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │ Email Provider   │  │   OAuth2         │  │   Stripe        │ │
│  │                  │  │   Providers      │  │   (existing)    │ │
│  │ - Gmail          │  │                  │  │                 │ │
│  │ - SendGrid       │  │ - Google         │  │ - Payments      │ │
│  │ - AWS SES        │  │ - GitHub         │  │ - Subscriptions │ │
│  │ - Mailgun        │  │ - Facebook       │  │                 │ │
│  │                  │  │ - Microsoft      │  │                 │ │
│  └──────────────────┘  └──────────────────┘  └─────────────────┘ │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## User Registration Flow

```
┌─────────┐
│  User   │
└────┬────┘
     │
     │ 1. Navigate to /register
     ▼
┌─────────────────────┐
│ Registration View   │
│                     │
│ [Username]          │
│ [Email]             │
│ [Password]          │
│ [Confirm Password]  │
│ [First Name]        │
│ [Last Name]         │
│ [✓] Terms & Cond.   │
│                     │
│   [Register]        │
└────┬────────────────┘
     │
     │ 2. Submit form
     ▼
┌─────────────────────┐
│ Registration        │
│   Service           │
│                     │
│ - Validate input    │
│ - Check duplicates  │
│ - Hash password     │
│ - Create user       │
│ - Generate token    │
└────┬────────────────┘
     │
     │ 3. Save to DB
     ▼
┌─────────────────────┐
│   PostgreSQL        │
│                     │
│ User:               │
│ - status: PENDING   │
│ - active: false     │
│ - emailVerified:    │
│   false             │
│                     │
│ Token:              │
│ - token: abc123...  │
│ - expiry: +7 days   │
└────┬────────────────┘
     │
     │ 4. Trigger email
     ▼
┌─────────────────────┐
│   Email Service     │
│                     │
│ - Load template     │
│ - Insert token link │
│ - Queue email       │
└────┬────────────────┘
     │
     │ 5. Send email
     ▼
┌─────────────────────┐
│  Email Provider     │
│  (Gmail/SendGrid)   │
│                     │
│ Subject:            │
│ "Verify your email" │
│                     │
│ Body:               │
│ "Click here to      │
│  verify: https://   │
│  app.com/verify?    │
│  token=abc123..."   │
└────┬────────────────┘
     │
     │ 6. User receives email
     ▼
┌─────────┐
│  User   │
│         │
│ Clicks  │
│  link   │
└────┬────┘
     │
     │ 7. GET /verify?token=abc123
     ▼
┌─────────────────────┐
│ Verification        │
│   Controller        │
│                     │
│ - Validate token    │
│ - Check expiry      │
│ - Check not used    │
└────┬────────────────┘
     │
     │ 8. Activate account
     ▼
┌─────────────────────┐
│   PostgreSQL        │
│                     │
│ User:               │
│ - status: ACTIVE    │
│ - active: true      │
│ - emailVerified:    │
│   true              │
│                     │
│ Token:              │
│ - used: true        │
└────┬────────────────┘
     │
     │ 9. Success message
     ▼
┌─────────────────────┐
│ Success View        │
│                     │
│ "✓ Email verified!" │
│ "You can now login" │
│                     │
│   [Go to Login]     │
└─────────────────────┘
```

---

## Login Flow (Username/Password)

```
┌─────────┐
│  User   │
└────┬────┘
     │
     │ 1. Navigate to /login
     ▼
┌─────────────────────┐
│   Login View        │
│                     │
│ [Username]          │
│ [Password]          │
│ [✓] Remember Me     │
│                     │
│   [Login]           │
│                     │
│ [Forgot Password?]  │
│ [Login with Google] │
│ [Login with GitHub] │
└────┬────────────────┘
     │
     │ 2. Submit credentials
     ▼
┌─────────────────────┐
│ Spring Security     │
│  Filter Chain       │
│                     │
│ 1. CSRF check       │
│ 2. Load user        │
│ 3. Verify password  │
│ 4. Check account    │
│    status           │
└────┬────────────────┘
     │
     │ 3. Query database
     ▼
┌─────────────────────┐
│   PostgreSQL        │
│                     │
│ SELECT * FROM       │
│ RENTAL_USER         │
│ WHERE username=?    │
│                     │
│ Check:              │
│ - active = true     │
│ - status = ACTIVE   │
│ - lockedUntil < now │
└────┬────────────────┘
     │
     ├─── Password incorrect ───┐
     │                          │
     │                          ▼
     │                    ┌─────────────────────┐
     │                    │ Failed Login        │
     │                    │   Handler           │
     │                    │                     │
     │                    │ - Increment counter │
     │                    │ - Log attempt       │
     │                    │ - Lock if >= 5      │
     │                    └────┬────────────────┘
     │                         │
     │                         ▼
     │                    ┌─────────────────────┐
     │                    │   Login History     │
     │                    │                     │
     │                    │ - success: false    │
     │                    │ - reason: "Invalid  │
     │                    │   credentials"      │
     │                    │ - ipAddress         │
     │                    │ - timestamp         │
     │                    └────┬────────────────┘
     │                         │
     │                         ▼
     │                    ┌─────────────────────┐
     │                    │   Error Message     │
     │                    │                     │
     │                    │ "Invalid            │
     │                    │  credentials"       │
     │                    └─────────────────────┘
     │
     │ Password correct
     ▼
┌─────────────────────┐
│ 2FA Enabled?        │
└────┬────────────────┘
     │
     ├─── Yes ──────────────────┐
     │                          │
     │                          ▼
     │                    ┌─────────────────────┐
     │                    │   2FA Prompt        │
     │                    │                     │
     │                    │ "Enter 6-digit code"│
     │                    │ [______]            │
     │                    │                     │
     │                    │ [Verify]            │
     │                    │ [Use backup code]   │
     │                    └────┬────────────────┘
     │                         │
     │                         │ 4. Verify code
     │                         ▼
     │                    ┌─────────────────────┐
     │                    │ 2FA Service         │
     │                    │                     │
     │                    │ - Load secret       │
     │                    │ - Generate TOTP     │
     │                    │ - Compare codes     │
     │                    └────┬────────────────┘
     │                         │
     │                         │ Code valid
     │                         │
     │ No                      │
     │                         │
     └─────────────────────────┘
                               │
                               │ 5. Create session
                               ▼
                         ┌─────────────────────┐
                         │ Session Service     │
                         │                     │
                         │ - Create session    │
                         │ - Store session ID  │
                         │ - Track device      │
                         │ - Log IP address    │
                         └────┬────────────────┘
                              │
                              │ 6. Save session
                              ▼
                         ┌─────────────────────┐
                         │   PostgreSQL        │
                         │                     │
                         │ UserSession:        │
                         │ - sessionId         │
                         │ - userId            │
                         │ - ipAddress         │
                         │ - userAgent         │
                         │ - createdDate       │
                         │ - active: true      │
                         │                     │
                         │ LoginHistory:       │
                         │ - success: true     │
                         │ - timestamp         │
                         │                     │
                         │ User:               │
                         │ - lastLoginDate     │
                         │ - failedAttempts: 0 │
                         └────┬────────────────┘
                              │
                              │ 7. Redirect
                              ▼
                         ┌─────────────────────┐
                         │   Dashboard         │
                         │                     │
                         │ "Welcome back,      │
                         │  John!"             │
                         └─────────────────────┘
```

---

## OAuth2 Login Flow (Google)

```
┌─────────┐
│  User   │
└────┬────┘
     │
     │ 1. Click "Login with Google"
     ▼
┌─────────────────────┐
│   Login View        │
│                     │
│ [Login with Google] │ ← Click
└────┬────────────────┘
     │
     │ 2. Redirect to Google
     ▼
┌─────────────────────┐
│ Spring Security     │
│  OAuth2 Client      │
│                     │
│ Redirect to:        │
│ https://accounts.   │
│ google.com/o/oauth2/│
│ auth?               │
│   client_id=...     │
│   redirect_uri=...  │
│   scope=openid,     │
│         profile,    │
│         email       │
└────┬────────────────┘
     │
     │ 3. User authorizes
     ▼
┌─────────────────────┐
│   Google OAuth2     │
│                     │
│ "Rental App wants   │
│  to access your:    │
│  - Email            │
│  - Profile          │
│                     │
│   [Allow] [Deny]    │
└────┬────────────────┘
     │
     │ 4. Callback with code
     ▼
┌─────────────────────┐
│ OAuth2 Callback     │
│   Controller        │
│                     │
│ GET /login/oauth2/  │
│ code/google?        │
│   code=abc123...    │
└────┬────────────────┘
     │
     │ 5. Exchange code for token
     ▼
┌─────────────────────┐
│ Spring Security     │
│  OAuth2 Client      │
│                     │
│ POST to Google:     │
│ - code              │
│ - client_id         │
│ - client_secret     │
│                     │
│ Response:           │
│ - access_token      │
│ - id_token          │
└────┬────────────────┘
     │
     │ 6. Get user info
     ▼
┌─────────────────────┐
│   Google API        │
│                     │
│ GET /userinfo       │
│ Authorization:      │
│   Bearer {token}    │
│                     │
│ Response:           │
│ {                   │
│   "sub": "12345",   │
│   "email": "...",   │
│   "name": "...",    │
│   "picture": "..."  │
│ }                   │
└────┬────────────────┘
     │
     │ 7. Map to local user
     ▼
┌─────────────────────┐
│ OAuth2 User         │
│   Service           │
│                     │
│ Check if user       │
│ exists by:          │
│ - provider: GOOGLE  │
│ - providerUserId    │
└────┬────────────────┘
     │
     ├─── User exists ──────────┐
     │                          │
     │                          ▼
     │                    ┌─────────────────────┐
     │                    │ Update user info    │
     │                    │                     │
     │                    │ - lastUsedDate      │
     │                    │ - profile picture   │
     │                    └────┬────────────────┘
     │                         │
     │                         │
     │ User doesn't exist      │
     │                         │
     ▼                         │
┌─────────────────────┐       │
│ Create new user     │       │
│                     │       │
│ User:               │       │
│ - username: email   │       │
│ - email: from OAuth │       │
│ - firstName         │       │
│ - lastName          │       │
│ - status: ACTIVE    │       │
│ - emailVerified:    │       │
│   true              │       │
│                     │       │
│ SocialConnection:   │       │
│ - provider: GOOGLE  │       │
│ - providerUserId    │       │
│ - email             │       │
│ - displayName       │       │
└────┬────────────────┘       │
     │                         │
     └─────────────────────────┘
                               │
                               │ 8. Create session
                               ▼
                         ┌─────────────────────┐
                         │ Session Service     │
                         │                     │
                         │ - Create session    │
                         │ - Log login         │
                         └────┬────────────────┘
                              │
                              │ 9. Redirect
                              ▼
                         ┌─────────────────────┐
                         │   Dashboard         │
                         │                     │
                         │ "Welcome, John!"    │
                         └─────────────────────┘
```

---

## Password Reset Flow

```
┌─────────┐
│  User   │
└────┬────┘
     │
     │ 1. Click "Forgot Password?"
     ▼
┌─────────────────────┐
│ Forgot Password     │
│      View           │
│                     │
│ "Enter your email"  │
│ [Email]             │
│                     │
│   [Send Reset Link] │
└────┬────────────────┘
     │
     │ 2. Submit email
     ▼
┌─────────────────────┐
│ Password Service    │
│                     │
│ - Validate email    │
│ - Check user exists │
│ - Generate token    │
└────┬────────────────┘
     │
     │ 3. Save token
     ▼
┌─────────────────────┐
│   PostgreSQL        │
│                     │
│ EmailVerification   │
│ Token:              │
│ - token: xyz789...  │
│ - type: PASSWORD_   │
│   RESET             │
│ - expiry: +1 hour   │
│ - used: false       │
└────┬────────────────┘
     │
     │ 4. Send email
     ▼
┌─────────────────────┐
│   Email Service     │
│                     │
│ Subject:            │
│ "Reset your         │
│  password"          │
│                     │
│ Body:               │
│ "Click here to      │
│  reset: https://    │
│  app.com/reset?     │
│  token=xyz789..."   │
│                     │
│ "Link expires in    │
│  1 hour"            │
│                     │
│ "If you didn't      │
│  request this,      │
│  ignore this email" │
└────┬────────────────┘
     │
     │ 5. User clicks link
     ▼
┌─────────┐
│  User   │
└────┬────┘
     │
     │ 6. GET /reset?token=xyz789
     ▼
┌─────────────────────┐
│ Password Reset      │
│   Controller        │
│                     │
│ - Validate token    │
│ - Check expiry      │
│ - Check not used    │
└────┬────────────────┘
     │
     ├─── Token invalid ────────┐
     │                          │
     │                          ▼
     │                    ┌─────────────────────┐
     │                    │   Error View        │
     │                    │                     │
     │                    │ "Invalid or expired │
     │                    │  reset link"        │
     │                    │                     │
     │                    │ [Request new link]  │
     │                    └─────────────────────┘
     │
     │ Token valid
     ▼
┌─────────────────────┐
│ Reset Password      │
│      View           │
│                     │
│ [New Password]      │
│ [Confirm Password]  │
│                     │
│ Password strength:  │
│ [████████░░] Strong │
│                     │
│   [Reset Password]  │
└────┬────────────────┘
     │
     │ 7. Submit new password
     ▼
┌─────────────────────┐
│ Password Service    │
│                     │
│ - Validate strength │
│ - Check history     │
│ - Hash password     │
│ - Update user       │
│ - Mark token used   │
│ - Invalidate        │
│   sessions          │
└────┬────────────────┘
     │
     │ 8. Update database
     ▼
┌─────────────────────┐
│   PostgreSQL        │
│                     │
│ User:               │
│ - password: new     │
│   hash              │
│ - passwordChanged   │
│   Date: now         │
│                     │
│ PasswordHistory:    │
│ - passwordHash      │
│ - changedDate       │
│                     │
│ Token:              │
│ - used: true        │
│                     │
│ UserSession:        │
│ - active: false     │
│   (all sessions)    │
└────┬────────────────┘
     │
     │ 9. Send confirmation email
     ▼
┌─────────────────────┐
│   Email Service     │
│                     │
│ Subject:            │
│ "Password changed"  │
│                     │
│ Body:               │
│ "Your password was  │
│  successfully       │
│  changed."          │
│                     │
│ "If this wasn't     │
│  you, contact       │
│  support            │
│  immediately."      │
└────┬────────────────┘
     │
     │ 10. Success message
     ▼
┌─────────────────────┐
│   Success View      │
│                     │
│ "✓ Password reset!" │
│ "You can now login  │
│  with your new      │
│  password"          │
│                     │
│   [Go to Login]     │
└─────────────────────┘
```

---

## 2FA Setup Flow

```
┌─────────┐
│  User   │
└────┬────┘
     │
     │ 1. Navigate to Profile → Security
     ▼
┌─────────────────────┐
│ Security Settings   │
│      View           │
│                     │
│ Two-Factor Auth:    │
│ Status: Disabled    │
│                     │
│   [Enable 2FA]      │
└────┬────────────────┘
     │
     │ 2. Click "Enable 2FA"
     ▼
┌─────────────────────┐
│ 2FA Service         │
│                     │
│ - Generate secret   │
│   key (Base32)      │
│ - Generate QR code  │
│ - Generate backup   │
│   codes             │
└────┬────────────────┘
     │
     │ 3. Display setup
     ▼
┌─────────────────────┐
│ 2FA Setup View      │
│                     │
│ "Scan QR code with  │
│  Google             │
│  Authenticator"     │
│                     │
│  ┌─────────────┐    │
│  │ QR CODE     │    │
│  │ [████████]  │    │
│  │ [████████]  │    │
│  └─────────────┘    │
│                     │
│ Or enter manually:  │
│ JBSWY3DPEHPK3PXP    │
│                     │
│ "Enter code to      │
│  verify:"           │
│ [______]            │
│                     │
│   [Verify]          │
└────┬────────────────┘
     │
     │ 4. User scans QR code
     │    with authenticator app
     │
     │ 5. User enters code
     ▼
┌─────────────────────┐
│ 2FA Service         │
│                     │
│ - Verify code       │
│ - Enable 2FA        │
│ - Save secret       │
└────┬────────────────┘
     │
     │ 6. Save to database
     ▼
┌─────────────────────┐
│   PostgreSQL        │
│                     │
│ TwoFactorAuth:      │
│ - userId            │
│ - secretKey         │
│ - enabled: true     │
│ - backupCodes:      │
│   ["12345678",      │
│    "87654321",      │
│    ...]             │
│ - createdDate       │
└────┬────────────────┘
     │
     │ 7. Show backup codes
     ▼
┌─────────────────────┐
│ Backup Codes View   │
│                     │
│ "Save these codes   │
│  in a safe place"   │
│                     │
│ 1. 12345678         │
│ 2. 87654321         │
│ 3. 23456789         │
│ 4. 98765432         │
│ 5. 34567890         │
│ 6. 09876543         │
│ 7. 45678901         │
│ 8. 10987654         │
│ 9. 56789012         │
│ 10. 21098765        │
│                     │
│ [Download] [Print]  │
│ [✓] I've saved them │
│                     │
│   [Done]            │
└────┬────────────────┘
     │
     │ 8. Confirmation
     ▼
┌─────────────────────┐
│ Security Settings   │
│      View           │
│                     │
│ Two-Factor Auth:    │
│ Status: ✓ Enabled   │
│                     │
│ [Disable 2FA]       │
│ [Regenerate Backup  │
│  Codes]             │
└─────────────────────┘
```

---

## Database Entity Relationships

```
┌─────────────────────────────────────────────────────────────────┐
│                         RENTAL_USER                             │
├─────────────────────────────────────────────────────────────────┤
│ PK  id                    UUID                                  │
│     username              VARCHAR(255) UNIQUE                   │
│     password              VARCHAR(255)                          │
│     email                 VARCHAR(255)                          │
│     firstName             VARCHAR(255)                          │
│     lastName              VARCHAR(255)                          │
│     active                BOOLEAN                               │
│     emailVerified         BOOLEAN                               │
│     accountStatus         VARCHAR(50)                           │
│     failedLoginAttempts   INTEGER                               │
│     lockedUntil           TIMESTAMP                             │
│     passwordChangedDate   TIMESTAMP                             │
│     lastLoginDate         TIMESTAMP                             │
│     createdDate           TIMESTAMP                             │
│     termsAccepted         BOOLEAN                               │
│     termsAcceptedDate     TIMESTAMP                             │
│     profilePictureUrl     VARCHAR(500)                          │
│     timeZoneId            VARCHAR(50)                           │
└────┬────────────────────────────────────────────────────────────┘
     │
     │ 1:N
     │
     ├──────────────────────────────────────────────────────────┐
     │                                                          │
     ▼                                                          ▼
┌─────────────────────────┐                    ┌─────────────────────────┐
│  PASSWORD_HISTORY       │                    │ EMAIL_VERIFICATION_     │
│                         │                    │        TOKEN            │
├─────────────────────────┤                    ├─────────────────────────┤
│ PK  id          UUID    │                    │ PK  id          UUID    │
│ FK  userId      UUID    │                    │ FK  userId      UUID    │
│     passwordHash        │                    │     token       VARCHAR │
│     changedDate         │                    │     tokenType   VARCHAR │
│     TIMESTAMP           │                    │     expiryDate          │
└─────────────────────────┘                    │     TIMESTAMP           │
                                               │     used        BOOLEAN │
                                               │     createdDate         │
                                               │     TIMESTAMP           │
                                               └─────────────────────────┘
     │
     │ 1:N
     │
     ├──────────────────────────────────────────────────────────┐
     │                                                          │
     ▼                                                          ▼
┌─────────────────────────┐                    ┌─────────────────────────┐
│    USER_SESSION         │                    │    LOGIN_HISTORY        │
│                         │                    │                         │
├─────────────────────────┤                    ├─────────────────────────┤
│ PK  id          UUID    │                    │ PK  id          UUID    │
│ FK  userId      UUID    │                    │ FK  userId      UUID    │
│     sessionId   VARCHAR │                    │     username    VARCHAR │
│     ipAddress   VARCHAR │                    │     ipAddress   VARCHAR │
│     userAgent   VARCHAR │                    │     userAgent   VARCHAR │
│     deviceInfo  VARCHAR │                    │     loginDate           │
│     location    VARCHAR │                    │     TIMESTAMP           │
│     createdDate         │                    │     success     BOOLEAN │
│     TIMESTAMP           │                    │     failureReason       │
│     lastActivityDate    │                    │     VARCHAR             │
│     TIMESTAMP           │                    │     location    VARCHAR │
│     active      BOOLEAN │                    └─────────────────────────┘
└─────────────────────────┘
     │
     │ 1:1
     │
     ├──────────────────────────────────────────────────────────┐
     │                                                          │
     ▼                                                          ▼
┌─────────────────────────┐                    ┌─────────────────────────┐
│   TWO_FACTOR_AUTH       │                    │ SOCIAL_LOGIN_           │
│                         │                    │    CONNECTION           │
├─────────────────────────┤                    ├─────────────────────────┤
│ PK  id          UUID    │                    │ PK  id          UUID    │
│ FK  userId      UUID    │                    │ FK  userId      UUID    │
│     UNIQUE              │                    │     provider    VARCHAR │
│     secretKey   VARCHAR │                    │     providerUserId      │
│     enabled     BOOLEAN │                    │     VARCHAR             │
│     backupCodes TEXT    │                    │     displayName VARCHAR │
│     createdDate         │                    │     email       VARCHAR │
│     TIMESTAMP           │                    │     profileUrl  VARCHAR │
│     lastUsedDate        │                    │     connectedDate       │
│     TIMESTAMP           │                    │     TIMESTAMP           │
└─────────────────────────┘                    │     lastUsedDate        │
                                               │     TIMESTAMP           │
                                               └─────────────────────────┘
     │
     │ 1:N
     │
     ▼
┌─────────────────────────┐
│  USER_SUBSCRIPTION      │
│  (existing)             │
├─────────────────────────┤
│ PK  id          UUID    │
│ FK  userId      UUID    │
│     planType    VARCHAR │
│     status      VARCHAR │
│     startDate           │
│     TIMESTAMP           │
│     endDate             │
│     TIMESTAMP           │
│     stripeCustomerId    │
│     VARCHAR             │
│     stripeSubscription  │
│     Id VARCHAR          │
└─────────────────────────┘
```

---

## Security Filter Chain

```
HTTP Request
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    1. CSRF Protection                           │
│                                                                 │
│  - Validate CSRF token                                          │
│  - Reject if invalid                                            │
└────┬────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    2. Session Management                        │
│                                                                 │
│  - Check session exists                                         │
│  - Validate session not expired                                 │
│  - Update last activity time                                    │
└────┬────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    3. Authentication                            │
│                                                                 │
│  - Load user from session                                       │
│  - OR authenticate with credentials                             │
│  - OR authenticate with OAuth2                                  │
└────┬────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    4. Account Status Check                      │
│                                                                 │
│  - Check account active                                         │
│  - Check not locked                                             │
│  - Check email verified (if required)                           │
└────┬────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    5. 2FA Verification                          │
│                                                                 │
│  - If 2FA enabled and not verified                              │
│  - Prompt for 2FA code                                          │
│  - Verify code                                                  │
└────┬────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    6. Authorization                             │
│                                                                 │
│  - Load user roles                                              │
│  - Check permissions                                            │
│  - Allow or deny access                                         │
└────┬────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    7. Audit Logging                             │
│                                                                 │
│  - Log access attempt                                           │
│  - Log user action                                              │
│  - Track changes                                                │
└────┬────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    8. Request Processing                        │
│                                                                 │
│  - Execute controller                                           │
│  - Process business logic                                       │
│  - Return response                                              │
└─────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Vaadin Flow UI 24.7.12                                         │
│  ├── Vaadin Components (TextField, Button, etc.)                │
│  ├── Vaadin Router (Navigation)                                 │
│  ├── Vaadin Security (CSRF, XSS protection)                     │
│  └── Custom Views (Registration, Login, Profile, etc.)          │
│                                                                 │
│  TypeScript 5.7.3                                               │
│  React 18.3.1 (for custom components)                           │
│  Lit 3.3.0 (for web components)                                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         BACKEND                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Jmix 2.6.2                                                     │
│  ├── jmix-core                                                  │
│  ├── jmix-security                                              │
│  ├── jmix-email ← NEW                                           │
│  ├── jmix-oidc ← NEW                                            │
│  ├── jmix-audit ← NEW                                           │
│  └── jmix-notifications (optional) ← NEW                        │
│                                                                 │
│  Spring Boot 3.x                                                │
│  ├── Spring Security 6.x                                        │
│  ├── Spring Data JPA                                            │
│  ├── Spring Web MVC                                             │
│  └── Spring Mail                                                │
│                                                                 │
│  Java 17+                                                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         SECURITY                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Spring Security 6.x                                            │
│  ├── BCrypt Password Encoder (strength: 12)                     │
│  ├── OAuth2 Client                                              │
│  ├── CSRF Protection                                            │
│  ├── Session Management                                         │
│  └── Security Filter Chain                                      │
│                                                                 │
│  Google Authenticator 1.5.0 ← NEW                               │
│  ├── TOTP generation                                            │
│  └── Code verification                                          │
│                                                                 │
│  ZXing 3.5.2 ← NEW                                              │
│  ├── QR code generation                                         │
│  └── Barcode support                                            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         DATABASE                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  PostgreSQL                                                     │
│  ├── User data                                                  │
│  ├── Security tokens                                            │
│  ├── Session data                                               │
│  ├── Audit logs                                                 │
│  └── Subscription data (existing)                               │
│                                                                 │
│  EclipseLink (JPA provider)                                     │
│  Liquibase (migrations)                                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      EXTERNAL SERVICES                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Email Provider                                                 │
│  ├── Gmail (testing)                                            │
│  ├── SendGrid (production)                                      │
│  ├── AWS SES (alternative)                                      │
│  └── Mailgun (alternative)                                      │
│                                                                 │
│  OAuth2 Providers                                               │
│  ├── Google OAuth2                                              │
│  ├── GitHub OAuth2                                              │
│  ├── Facebook (optional)                                        │
│  └── Microsoft (optional)                                       │
│                                                                 │
│  Stripe (existing)                                              │
│  └── Payment processing                                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         PRODUCTION                              │
└─────────────────────────────────────────────────────────────────┘

                              Internet
                                 │
                                 │ HTTPS
                                 ▼
                    ┌────────────────────────┐
                    │   Load Balancer        │
                    │   (AWS ALB / Nginx)    │
                    └────────┬───────────────┘
                             │
                ┌────────────┼────────────┐
                │            │            │
                ▼            ▼            ▼
        ┌───────────┐ ┌───────────┐ ┌───────────┐
        │  App      │ │  App      │ │  App      │
        │  Server 1 │ │  Server 2 │ │  Server 3 │
        │           │ │           │ │           │
        │  Jmix App │ │  Jmix App │ │  Jmix App │
        │  :8080    │ │  :8080    │ │  :8080    │
        └─────┬─────┘ └─────┬─────┘ └─────┬─────┘
              │             │             │
              └─────────────┼─────────────┘
                            │
                            ▼
                ┌───────────────────────┐
                │   PostgreSQL          │
                │   (Primary)           │
                │                       │
                │   - User data         │
                │   - Sessions          │
                │   - Audit logs        │
                └───────┬───────────────┘
                        │
                        │ Replication
                        ▼
                ┌───────────────────────┐
                │   PostgreSQL          │
                │   (Replica)           │
                │   (Read-only)         │
                └───────────────────────┘

        ┌───────────────────────┐
        │   Redis               │
        │   (Session Store)     │
        │                       │
        │   - Shared sessions   │
        │   - Rate limiting     │
        │   - Caching           │
        └───────────────────────┘

        ┌───────────────────────┐
        │   Email Service       │
        │   (SendGrid/AWS SES)  │
        │                       │
        │   - Transactional     │
        │   - Notifications     │
        └───────────────────────┘

        ┌───────────────────────┐
        │   OAuth2 Providers    │
        │                       │
        │   - Google            │
        │   - GitHub            │
        └───────────────────────┘

        ┌───────────────────────┐
        │   Stripe              │
        │   (Payments)          │
        └───────────────────────┘

        ┌───────────────────────┐
        │   Monitoring          │
        │                       │
        │   - Logs (CloudWatch) │
        │   - Metrics (Grafana) │
        │   - Alerts            │
        └───────────────────────┘
```

---

**Last Updated**: 2024  
**Status**: ✅ ARCHITECTURE COMPLETE  
**Next**: Review and approve design