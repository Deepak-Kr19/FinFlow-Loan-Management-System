# 🏦 FinFlow Frontend — Complete Technical Walkthrough

> **Angular 21 | Standalone Components | Signals | Reactive Forms | JWT Auth**
> 
> A production-ready fintech loan management frontend integrating with a Spring Boot microservices backend.

---

## 📋 Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Architecture & Folder Structure](#3-architecture--folder-structure)
4. [Design System](#4-design-system)
5. [Core Layer — Models, Services, Guards, Interceptors](#5-core-layer)
6. [Shared Components](#6-shared-components)
7. [Feature Modules](#7-feature-modules)
8. [Routing & Navigation](#8-routing--navigation)
9. [Authentication Flow](#9-authentication-flow)
10. [Loan Application Lifecycle](#10-loan-application-lifecycle)
11. [Admin Workflow](#11-admin-workflow)
12. [Backend Integration](#12-backend-integration)
13. [Build & Performance](#13-build--performance)
14. [How to Run](#14-how-to-run)

---

## 1. Project Overview

FinFlow is a **loan onboarding and approval system** with two user roles:

| Role | Capabilities |
|------|-------------|
| **Applicant** | Register, login, create multi-step loan applications, upload documents, track status |
| **Admin** | View all applications, verify documents, approve/reject loans, manage users, view reports |

### Key Frontend Features
- ✅ **Multi-step loan application wizard** (3 steps with step indicator)
- ✅ **Visual status timeline** (Draft → Submitted → Approved/Rejected)
- ✅ **Document upload & download** with verification status
- ✅ **Role-based routing** (separate dashboards for Admin vs Applicant)
- ✅ **JWT authentication** with global 401 interceptor
- ✅ **Angular Signals** for reactive state management
- ✅ **Reactive Forms** with validation on all inputs
- ✅ **Dark fintech theme** with glassmorphism and micro-animations
- ✅ **Lazy-loaded routes** (15 pages, all code-split)

---

## 2. Technology Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Angular | 21.2.0 | Core framework |
| TypeScript | 5.9.2 | Type safety |
| RxJS | 7.8.x | HTTP calls, async streams |
| Angular Signals | Built-in | Reactive state (replaces BehaviorSubjects) |
| SCSS | Native | Design system & styling |
| Inter Font | Google Fonts | Typography |

### No External UI Libraries
The entire UI is built from scratch — **no Angular Material, no PrimeNG, no Bootstrap**. Every component, button, card, table, badge, and animation is hand-crafted in the global design system.

---

## 3. Architecture & Folder Structure

```
finflow-ui/
├── src/
│   ├── index.html                    # SEO-optimized entry point
│   ├── main.ts                       # Bootstrap with appConfig
│   ├── styles.scss                   # Global design system (294 lines)
│   └── app/
│       ├── app.component.ts          # Root — conditional layout rendering
│       ├── app.config.ts             # Providers: Router + HttpClient + Interceptor
│       ├── app.routes.ts             # 15 lazy-loaded routes with guards
│       │
│       ├── core/                     # 🧠 Singleton services & infrastructure
│       │   ├── models/
│       │   │   ├── user.model.ts         # User, LoginRequest, RegisterRequest, AuthResponse
│       │   │   ├── application.model.ts  # LoanApplication, ApplicationRequest, ApplicationStatus
│       │   │   ├── document.model.ts     # Document, DOC_TYPES
│       │   │   └── decision.model.ts     # Decision
│       │   ├── services/
│       │   │   ├── auth.service.ts       # JWT + Signals (currentUser, isAdmin, isApplicant)
│       │   │   ├── application.service.ts # CRUD + submit + getById
│       │   │   ├── document.service.ts   # Upload, download, verify
│       │   │   ├── admin.service.ts      # Applications, decisions, users, reports
│       │   │   └── toast.service.ts      # Signal-based notification system
│       │   ├── interceptors/
│       │   │   └── auth.interceptor.ts   # JWT attach + global 401 redirect
│       │   └── guards/
│       │       ├── auth.guard.ts         # Token existence check
│       │       └── role.guard.ts         # Admin/Applicant role check
│       │
│       ├── shared/                   # 🔧 Reusable UI components
│       │   └── components/
│       │       ├── layout/               # Sidebar + navigation + user avatar
│       │       ├── toast/                # Notification toasts (success/error/info/warning)
│       │       └── status-timeline/      # Visual loan lifecycle indicator
│       │
│       └── features/                # 📄 Page components (lazy-loaded)
│           ├── auth/
│           │   ├── login/                # Login page with reactive form
│           │   └── register/             # Registration with role selection
│           ├── profile/                  # User profile page
│           ├── applicant/
│           │   ├── dashboard/            # Stats + quick actions + recent apps
│           │   ├── application-wizard/   # 3-step multi-step form
│           │   ├── my-applications/      # Application list with submit action
│           │   └── application-detail/   # Timeline + documents + upload
│           └── admin/
│               ├── admin-dashboard/      # Stats + pending queue
│               ├── all-applications/     # Searchable/filterable list
│               ├── application-review/   # Doc verify + approve/reject decision
│               ├── user-management/      # User list with role stats
│               └── reports/              # System reports table
│
├── proxy.conf.json               # Dev proxy → API Gateway (localhost:8080)
├── angular.json                  # Build config with proxyConfig wired
└── package.json                  # Dependencies (zero UI library deps)
```

### Architecture Principles
1. **Standalone Components Only** — No NgModules anywhere
2. **Core/Shared/Features** — Clean separation of concerns
3. **Lazy Loading** — Every page is a separate chunk
4. **Signals over BehaviorSubjects** — Modern Angular reactive primitives
5. **Reactive Forms over Template-Driven** — Programmatic validation

---

## 4. Design System

The entire UI is powered by a **custom design system** defined in `styles.scss` (294 lines).

### Color Palette (Dark Fintech Theme)

| Token | Value | Usage |
|-------|-------|-------|
| `--bg-primary` | `#080c14` | Page background |
| `--bg-secondary` | `#0f1520` | Sidebar background |
| `--bg-tertiary` | `#161d2e` | Card backgrounds |
| `--accent` | `#6366f1` | Primary brand (Indigo) |
| `--accent-light` | `#818cf8` | Hover states, links |
| `--green` | `#22c55e` | Approved, success |
| `--orange` | `#f59e0b` | Submitted, pending |
| `--red` | `#ef4444` | Rejected, errors |
| `--cyan` | `#06b6d4` | Draft status |

### Component Classes

| Class | Description |
|-------|-------------|
| `.btn-primary` | Gradient indigo-to-violet with glow on hover |
| `.btn-success` | Green gradient with shadow lift |
| `.btn-danger` | Red gradient for destructive actions |
| `.btn-secondary` | Subtle border button |
| `.btn-outline` | Transparent with accent border |
| `.card` | Glass-effect card with border + hover |
| `.badge-*` | Color-coded status pills (draft/submitted/approved/rejected) |
| `.stat-card` | Dashboard metric card with gradient text |
| `.form-control` | Input with focus glow ring |
| `.table-container` | Scrollable table with accent headers |
| `.spinner` | CSS-only loading spinner |
| `.fade-in` | Page entrance animation |

### Typography
- **Font**: Inter (Google Fonts) — weights 300–900
- **Headings**: Gradient text (`#fff → accent-light`) with tight letter-spacing
- **Labels**: Uppercase, 0.78rem, muted color, 0.06em spacing

### Responsive Breakpoints
- `768px` — Grids collapse to single column, sidebar hides, padding reduces

---

## 5. Core Layer

### 5.1 Models

**`user.model.ts`** — Defines 4 interfaces:
```typescript
User          → { id, name, email, role }
LoginRequest  → { email, password }
RegisterRequest → { name, email, password, role }
AuthResponse  → { token, role, userId, name, email }
```

**`application.model.ts`** — Loan application with typed status:
```typescript
LoanApplication → { id, userId, personalDetails, employmentDetails, loanDetails, status }
ApplicationStatus → 'Draft' | 'Submitted' | 'APPROVED' | 'REJECTED'
STATUS_FLOW → ['Draft', 'Submitted', 'APPROVED']  // For timeline rendering
```

**`document.model.ts`** — Document with type constants:
```typescript
Document → { id, applicationId, type, filePath, status: 'PENDING'|'VERIFIED'|'REJECTED' }
DOC_TYPES → [ID_PROOF, ADDRESS_PROOF, SALARY_SLIP, BANK_STATEMENT, PAN_CARD]
```

### 5.2 Services

#### AuthService — Signal-Based State Management
```
┌─────────────────────────────────────────────────┐
│  AuthService (providedIn: 'root')               │
│                                                 │
│  Signals:                                       │
│    currentUser  → signal<{userId,name,...}|null> │
│    isLoggedIn   → computed(() => !!currentUser)  │
│    currentRole  → computed(() => role)           │
│    isAdmin      → computed(() => role=ADMIN)     │
│    isApplicant  → computed(() => role=APPLICANT) │
│                                                 │
│  Methods:                                       │
│    login()    → POST /auth/login → store JWT    │
│    register() → POST /auth/signup               │
│    logout()   → clear storage, redirect /login  │
│    getProfile() → GET /auth/profile             │
│    getToken() → read from localStorage          │
│    hasToken() → boolean check                   │
│                                                 │
│  Storage Keys:                                  │
│    finflow_token → JWT string                   │
│    finflow_user  → JSON {userId,name,email,role}│
└─────────────────────────────────────────────────┘
```

#### ApplicationService — CRUD + Submit
| Method | HTTP | Endpoint |
|--------|------|----------|
| `create(req)` | POST | `/applications` |
| `getMyApplications()` | GET | `/applications/my` |
| `getById(id)` | GET | `/applications/{id}` |
| `update(id, req)` | PUT | `/applications/{id}` |
| `submit(id)` | POST | `/applications/{id}/submit` |
| `getStatus(id)` | GET | `/applications/{id}/status` |

#### DocumentService — Upload, Download, Verify
| Method | HTTP | Endpoint |
|--------|------|----------|
| `upload(appId, type, file)` | POST | `/documents/upload` (multipart) |
| `getByApplicationId(appId)` | GET | `/documents/application/{id}` |
| `verify(docId, status)` | PUT | `/documents/{id}/verify` |
| `download(docId, fileName)` | GET | `/documents/{id}/download` (blob) |

#### AdminService — Inter-Service Proxy
| Method | HTTP | Endpoint |
|--------|------|----------|
| `getAllApplications()` | GET | `/admin/applications` |
| `makeDecision(id, decision, remarks)` | POST | `/admin/applications/{id}/decision` |
| `getReports()` | GET | `/admin/reports` |
| `getAllUsers()` | GET | `/admin/users` |
| `updateUser(id, user)` | PUT | `/admin/users/{id}` |

#### ToastService — Signal-Based Notifications
```typescript
// Uses signal<Toast[]> for reactive toast stack
toastService.success('Application submitted!');
toastService.error('Invalid credentials');
toastService.info('Welcome back');
toastService.warning('Document pending verification');
// Auto-dismiss after 4 seconds
```

### 5.3 HTTP Interceptor

**`auth.interceptor.ts`** — Functional interceptor (Angular 21 pattern):
```
Every HTTP Request
    ↓
Is it /auth/login or /auth/signup?
    ├── YES → Pass through unchanged
    └── NO → Clone request, add Authorization: Bearer <token>
              ↓
         Response received
              ↓
         Is status 401?
              ├── YES → Clear localStorage → Navigate to /login
              └── NO → Pass response through
```

### 5.4 Route Guards

| Guard | Logic |
|-------|-------|
| `authGuard` | Checks `hasToken()`. If false → redirect `/login` |
| `adminGuard` | Checks `hasToken() && isAdmin()`. If false → redirect `/dashboard` |
| `applicantGuard` | Checks `hasToken() && isApplicant()`. If false → redirect `/admin` |

---

## 6. Shared Components

### 6.1 Layout Component (`layout.component.ts`)
- Fixed **260px sidebar** with dark secondary background
- **Brand logo** with gradient text "FinFlow"
- **Role-based navigation** — shows different links for Admin vs Applicant
- **User avatar pill** — shows first letter of name with gradient background
- **Logout button** — hover turns red with icon

### 6.2 Toast Component (`toast.component.ts`)
- Fixed position top-right stack
- 4 types: success (green), error (red), warning (orange), info (indigo)
- Slide-in animation, backdrop-filter blur
- Click to dismiss, auto-dismiss after 4s

### 6.3 Status Timeline (`status-timeline.component.ts`)
- Visual horizontal dots connected by lines
- Steps: Draft → Submitted → Approved / Rejected
- Completed steps: green dot with ✓
- Active step: indigo glow with pulse
- Rejected: red dot with ✕

---

## 7. Feature Modules

### 7.1 Authentication Pages

#### Login Page
- Centered glassmorphism card with backdrop blur
- Background orbs (gradient circles) for visual depth
- Reactive form: email (required, email format) + password (required)
- Loading signal: shows spinner inside button during API call
- On success: redirect to `/dashboard` (applicant) or `/admin` (admin)

#### Register Page
- Same visual style as login
- Fields: name, email, password (min 6 chars), role (dropdown)
- Default role: `ROLE_APPLICANT`
- On success: toast + redirect to `/login`

### 7.2 Profile Page
- Two-column layout: avatar card + account details
- Large avatar with gradient background showing initials
- Role badge (green for Admin, cyan for Applicant)
- Detail rows: User ID, Full Name, Email, Role

### 7.3 Applicant Features

#### Dashboard
- **Welcome greeting** with user's name
- **4 stat cards**: Total, Drafts, Submitted, Approved
- **Quick Actions panel**: New Application, My Applications, Profile
- **Recent Applications list** (latest 4 with status badges)

#### Application Wizard (3-Step Multi-Step Form)
```
Step 1: Personal Details          Step 2: Employment          Step 3: Loan Details
┌─────────────────────┐     ┌────────────────────┐     ┌──────────────────────┐
│ Full Name           │     │ Company Name       │     │ Loan Amount (₹)      │
│ Date of Birth       │     │ Designation        │     │ Tenure (Months)      │
│ Phone Number        │     │ Monthly Salary (₹) │     │ Loan Type (select)   │
│ PAN Number          │     │ Experience (years) │     │ Purpose (textarea)   │
│ Address (textarea)  │     │ Employment Type    │     │                      │
└─────────────────────┘     └────────────────────┘     └──────────────────────┘
```
- **Step indicator** at top with connected dots (green = done, indigo = active)
- Click on completed step numbers to jump back
- JSON serialization of each section for backend storage
- Edit mode: loads existing application data and pre-fills fields

#### My Applications
- Table with columns: ID, Loan Summary, Status, Actions
- **Smart JSON parsing**: shows "Home Loan — ₹50,00,000" instead of raw JSON
- Draft applications show **Edit** and **Submit** buttons
- Empty state with illustration and "New Application" CTA

#### Application Detail
- **Status Timeline** at top showing current lifecycle position
- **3 detail cards**: Personal, Employment, Loan (JSON auto-formatted)
- **Document section**: upload form (type selector + file input) + document list
- Each document shows: type, filename, status badge, **📥 View/Download** button

### 7.4 Admin Features

#### Admin Dashboard
- **4 stat cards**: Total Apps, Pending, Approved, Rejected
- **Quick Actions**: Review Applications, Manage Users, Reports
- **Pending Queue**: latest 5 submitted applications with review links

#### All Applications
- **Search bar** with real-time text filtering
- **Status filter buttons**: All, Pending, Approved, Rejected
- Table: ID, User, Loan Summary, Status, Review button

#### Application Review
- **Status Timeline** showing application position
- **3 detail cards** (Personal, Employment, Loan)
- **Documents section** with per-document:
  - 📥 View/Download button
  - Status badge
  - ✓ Verify / ✕ Reject buttons (for PENDING docs)
- **Decision Panel** (sticky sidebar):
  - Two large toggle buttons: ✅ Approve / ❌ Reject
  - Remarks textarea (required)
  - Submit Decision button

#### User Management
- **3 stat cards**: Total Users, Applicants, Admins
- Table: ID, Name, Email, Role (with colored badges)

#### Reports
- Table: ID, Type, Data
- Empty state when no reports exist

---

## 8. Routing & Navigation

### Route Map (15 routes)

| Path | Component | Guards | Role |
|------|-----------|--------|------|
| `/` | → redirect `/login` | — | — |
| `/login` | LoginComponent | — | Public |
| `/register` | RegisterComponent | — | Public |
| `/dashboard` | DashboardComponent | auth + applicant | Applicant |
| `/applications` | MyApplicationsComponent | auth + applicant | Applicant |
| `/applications/new` | ApplicationWizardComponent | auth + applicant | Applicant |
| `/applications/edit/:id` | ApplicationWizardComponent | auth + applicant | Applicant |
| `/applications/:id` | ApplicationDetailComponent | auth + applicant | Applicant |
| `/profile` | ProfileComponent | auth | Both |
| `/admin` | AdminDashboardComponent | auth + admin | Admin |
| `/admin/applications` | AllApplicationsComponent | auth + admin | Admin |
| `/admin/applications/:id` | ApplicationReviewComponent | auth + admin | Admin |
| `/admin/users` | UserManagementComponent | auth + admin | Admin |
| `/admin/reports` | ReportsComponent | auth + admin | Admin |
| `**` | → redirect `/login` | — | — |

### Conditional Layout
```
AppComponent checks: hasToken() && not on /login or /register
  ├── YES → Show sidebar layout + <router-outlet>
  └── NO  → Show only <router-outlet> (full-screen auth pages)
```

---

## 9. Authentication Flow

```
┌──────────┐    POST /auth/login     ┌──────────────┐    Route via    ┌─────────────┐
│  Login   │ ──────────────────────→ │ API Gateway  │ ──────────────→ │ Auth Service│
│  Page    │                         │  :8080       │                 │  :8081      │
└──────────┘                         └──────────────┘                 └─────────────┘
     │                                                                      │
     │  ← { token, role, userId, name, email }                             │
     │                                                                      │
     ▼
┌────────────────────────────────────────────────┐
│  AuthService.login() runs tap() operator:      │
│  1. Store token in localStorage                │
│  2. Store user info in localStorage            │
│  3. Update currentUser signal                  │
│  4. Navigate to /dashboard or /admin           │
└────────────────────────────────────────────────┘
     │
     ▼  (All subsequent requests)
┌────────────────────────────────────────────────┐
│  authInterceptor attaches:                     │
│  Authorization: Bearer <token>                 │
│                                                │
│  On 401 response:                              │
│  → Clear localStorage                          │
│  → Navigate to /login                          │
└────────────────────────────────────────────────┘
```

---

## 10. Loan Application Lifecycle

```
  ┌─────────┐     Create      ┌───────────┐     Submit      ┌───────────┐
  │  Wizard │ ──────────────→ │   Draft   │ ──────────────→ │ Submitted │
  │ (3 step)│    POST /apps   │  (editable)│  POST /submit  │ (locked)  │
  └─────────┘                 └───────────┘                 └───────────┘
                                    │                              │
                                    │ Upload docs                  │ Admin reviews
                                    ▼                              ▼
                              ┌───────────┐              ┌──────────────────┐
                              │ Documents │              │ Application      │
                              │ PENDING   │              │ Review Page      │
                              └───────────┘              │ • Verify docs    │
                                                         │ • Make decision  │
                                                         └──────────────────┘
                                                                │
                                                    ┌───────────┴───────────┐
                                                    ▼                       ▼
                                              ┌──────────┐          ┌──────────┐
                                              │ APPROVED │          │ REJECTED │
                                              │  (green) │          │  (red)   │
                                              └──────────┘          └──────────┘
```

### Data Flow for Application Creation:
1. User fills 3-step wizard form
2. Each section is `JSON.stringify()`'d → `personalDetails`, `employmentDetails`, `loanDetails`
3. Sent as `ApplicationRequest` to `POST /applications`
4. Backend creates with status `Draft`
5. On detail page, JSON is parsed back for display: `fullName: John Doe`, `salary: 75000`

---

## 11. Admin Workflow

1. **Login as Admin** → redirected to `/admin`
2. **Dashboard** shows pending count + quick links
3. **All Applications** → search/filter → click "Review"
4. **Review Page**:
   - View applicant's personal, employment, loan details
   - View uploaded documents → click 📥 to download
   - Verify each document (✓ or ✕)
   - Select Approve/Reject → add remarks → submit decision
5. Decision is sent via `POST /admin/applications/{id}/decision`
6. Backend publishes `DECISION_MADE` event to RabbitMQ
7. Application Service updates status to `APPROVED` / `REJECTED`
8. Applicant sees updated status on their dashboard

---

## 12. Backend Integration

### API Proxy Configuration (`proxy.conf.json`)
```json
{
  "/auth":         { "target": "http://localhost:8080" },
  "/applications": { "target": "http://localhost:8080" },
  "/documents":    { "target": "http://localhost:8080" },
  "/admin":        { "target": "http://localhost:8080" }
}
```
All frontend requests → **API Gateway (:8080)** → routes to correct microservice.

### Request/Response Flow
```
Angular App (:4200)
    │ proxy
    ▼
API Gateway (:8080) → JWT validation → inject X-User-Id, X-User-Role headers
    │
    ├──→ Auth Service     (:8081) — /auth/**
    ├──→ Application Svc  (:8082) — /applications/**
    ├──→ Document Svc     (:8083) — /documents/**
    └──→ Admin Service    (:8084) — /admin/**
```

---

## 13. Build & Performance

### Production Build Output
```
Initial Chunk Files           | Raw Size  | Transfer Size
──────────────────────────────┼───────────┼──────────────
chunk (vendor/RxJS)           | 167.26 kB |  48.17 kB
chunk (Angular core)          | 95.22 kB  |  23.95 kB
styles (design system)        | 22.10 kB  |   2.60 kB
main (bootstrap)              |  9.69 kB  |   2.81 kB
──────────────────────────────┼───────────┼──────────────
Initial Total                 | 296.15 kB |  78.67 kB

Lazy Chunks (15 pages)        | Raw Size  | Transfer Size
──────────────────────────────┼───────────┼──────────────
Reactive Forms module         | 41.53 kB  |   8.71 kB
Application Wizard            |  9.29 kB  |   2.83 kB
Application Review            |  7.04 kB  |   2.45 kB
Application Detail            |  5.88 kB  |   2.21 kB
+ 11 more page chunks...     |           |
```

### Performance Highlights
- **78.67 kB initial transfer** — fast first paint
- **15 lazy-loaded chunks** — pages load on demand
- **Zero external UI libraries** — no unused CSS/JS
- **CSS animations only** — no JS animation libraries

---

## 14. How to Run

### Prerequisites
- Node.js 18+, npm 10+
- Backend services running (via Docker Compose or manually)

### Development Server
```bash
cd finflow-ui
npm install
ng serve
# Opens at http://localhost:4200
# Proxy routes API calls to http://localhost:8080 (API Gateway)
```

### Production Build
```bash
ng build
# Output: dist/finflow-ui/
```

### Full Stack (Docker)
```bash
cd FinFlow-Microservices
docker-compose up --build -d
# Then run frontend separately:
cd finflow-ui && ng serve
```

### Test Credentials (after registration)
1. Register as **Applicant**: any email, role = ROLE_APPLICANT
2. Register as **Admin**: any email, role = ROLE_ADMIN
3. Login → auto-redirected to role-specific dashboard

---

## File Summary

| Category | Files | Key Technologies |
|----------|-------|-----------------|
| Models | 4 | TypeScript interfaces, union types |
| Services | 5 | HttpClient, Signals, RxJS, Blob download |
| Guards | 2 | CanActivateFn, inject() |
| Interceptors | 1 | HttpInterceptorFn, catchError |
| Shared Components | 3 | Layout, Toast, StatusTimeline |
| Auth Pages | 2 | ReactiveFormsModule, Validators |
| Profile | 1 | Signal-based loading state |
| Applicant Pages | 4 | Multi-step form, JSON serialization |
| Admin Pages | 5 | Search/filter, doc verification, decision |
| Config | 4 | Routes, AppConfig, Proxy, Styles |
| **Total** | **31 files** | |

---

> **Author**: Deepak Kumar  
> **Framework**: Angular 21.2.0  
> **Build Status**: ✅ Zero errors, 78.67 kB initial bundle  
> **Last Updated**: April 2026
