You are a senior frontend engineer + product designer. Build a complete production-ready frontend for a crowdfunding platform with three roles:

1. INVESTOR
2. CUSTOMER (Project Creator)
3. ADMIN

The backend is fully implemented (Spring Boot REST APIs). Build a scalable, clean, and user-centric frontend with real-world UX.

---

# 🔧 TECH STACK (MANDATORY)

* React (functional components + hooks)
* Tailwind CSS
* Zustand (state management)
* Axios (API calls)
* React Router

---

# 📂 FOLDER STRUCTURE

/src
/components
/pages
/layouts
/store (Zustand)
/services
/hooks
/utils

---

# 🔐 AUTHENTICATION

* JWT-based login
* Token stored in localStorage
* Axios interceptor for Authorization header
* Protected routes

### ROLES:

* INVESTOR
* CUSTOMER
* ADMIN (login only, no registration)

---

# 🧱 GLOBAL LAYOUT

🏠 HOME PAGE (VERY IMPORTANT)

Design a strong landing page:

Sections:
🔹 Hero Section
Platform tagline
CTA: "Explore Projects"
CTA: "Start Your Project"
🔹 About Preview (SHORT)
2–3 lines about platform
Button: "Know More" → /about
🔹 Featured Projects
Top projects with progress bars
🔹 Testimonials Preview

Show 3–4 cards:

User name
Role (Investor / Customer)
Feedback text

Button:
👉 "View All Testimonials" → /testimonials

🔹 FAQ Preview

Show 3–4 common questions

Button:
👉 "View All FAQs" → /faq

📄 ABOUT PAGE (/about)
Full platform description
Mission
Vision
How it works (step-by-step)
Roles explanation:
Investor
Customer
Admin
💬 TESTIMONIALS PAGE (/testimonials)

Display:

User name
Role
Feedback
Optional rating ⭐

Layout:

Grid cards

Optional:

Filter by role (Investor / Customer)
❓ FAQ PAGE (/faq)

Accordion UI:

Examples:

How to invest?
How approval works?
Is investment safe?
How equity works?

Expandable sections

### Header

* Logo
* Role-based navigation
* Profile dropdown
* Logout

### Footer

* About
* Contact
* Terms

---

# 👥 ROLE-BASED FEATURES

---

# 💰 INVESTOR FLOW

---

## 🔹 1. Browse Projects

GET /projects

Show:

* Title
* Description
* Goal vs Current funding
* Equity offered
* Progress bar

---

## 🔹 2. Project Detail Page

Sections:

### Project Info

* Title
* Description
* Deadline
* Funding progress

---

### Investment Transparency

Show:

* List of investors
* Amount invested
* Equity taken

---

### Engagement

* Likes
* Shares
* Comments
* Investor count

---

### Chat Section

* Chat with project creator

---

### Actions

* Like
* Comment
* Share
* Report Issue

---

## 🔹 3. Send Investment Request

POST /api/investment-request

Inputs:

* Amount
* Equity %

---

## 🔹 4. Investor Dashboard (IMPORTANT)

GET /api/investment-request/investor

Show TWO SECTIONS:

### 🟡 Requested Investments

* Project
* Amount
* Equity
* Status:

  * PENDING
  * APPROVED
  * REJECTED

---

### 🟢 Completed Investments

(After payment success)

* Project
* Amount invested
* Equity owned
* Investment date

---

## 🔹 5. Payment Flow

If request is APPROVED:

POST /api/payments/create-order
→ Razorpay Checkout
→ POST /api/payments/verify

---

# 🧑‍💼 CUSTOMER FLOW (PROJECT CREATOR)

---

## 🔹 1. Create / List Project (CRITICAL FEATURE)

POST /api/projects

Form fields:

* Title
* Description
* Goal amount
* Total equity offered
* Deadline

---

## 🔹 2. Customer Dashboard

Show:

### 📁 My Projects

GET /projects?creatorId=...

Each project card shows:

* Title
* Funding progress
* Equity sold vs remaining

---

## 🔹 3. Project Analytics (VERY IMPORTANT)

Inside each project:

* Total investors count
* Total funding received
* Remaining funding
* Equity sold vs remaining
* Engagement:

  * Likes
  * Shares
  * Comments

---

## 🔹 4. Investment Requests Management

GET /api/investment-request/customer

Show:

* Investor name
* Amount
* Equity
* Status

Actions:

* Approve
* Reject

---

## 🔹 5. Project Investment Report

Customer can see:

* List of all investors
* Amount each invested
* Equity distributed
* Total performance summary

---

# 🛡️ ADMIN PANEL

---

## 🔐 Admin Login Only

---

## 📊 Admin Dashboard

Show:

* Total projects
* Total platform funding
* Total investors
* Total customers
* Active projects
* Top performing projects

---

## 📁 Manage Projects

Admin can:

* View all projects
* Delete project (with reason)

---

## 🚨 Reports Management

Admin sees:

* Report reason
* Project details
* Reporter

Actions:

* Ignore
* Take action

---

## 🔒 User Control

Admin can:

* Block customer
* Block investor
* Set block duration

---

# 🚨 REPORT SYSTEM

From project page:

User selects:

* Customer not replying
* Abuse
* Violation
* Other

Then:

* Text input
* Submit

---

# 🎨 UI/UX REQUIREMENTS

* Modern SaaS UI (Stripe-like)
* Fully responsive
* Cards, tables, modals
* Status badges
* Toast notifications
* Loading states
* Empty states

---

# 🧠 STATE MANAGEMENT (ZUSTAND)

Store:

* Auth user + role
* Projects
* Investment requests
* Investments
* Payments
* Chat
* Admin data

---

# ⚡ API RULES

* Base URL from .env
* Axios instance
* Auto attach token
* Global error handling

---

# 🔐 SECURITY RULES

* Only investor can invest
* Only customer can approve/reject
* Only admin can delete/block
* Disable invalid UI actions

---

# 🚀 ADVANCED FEATURES

* Filtering (status-based)
* Sorting (latest first)
* Dashboard charts
* Confirmation modals

---

# 🎯 FINAL EXPECTATION

This should behave like a real startup product:

✔ Investor → explore → request → track → pay
✔ Customer → create → manage → analyze
✔ Admin → monitor → control
✔ Full transparency in investment
✔ Clean UX + scalable architecture

Do NOT generate dummy UI. Build real, production-ready frontend.
