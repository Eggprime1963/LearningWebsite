# API Reference - Learning Management System

## Authentication Endpoints

### POST /login
Standard user authentication endpoint.

**Request Body:**
```json
{
  "username": "user@example.com",
  "password": "securepassword"
}
```

**Response:**
```json
{
  "success": true,
  "redirect": "/home",
  "user": {
    "id": 1,
    "username": "user@example.com",
    "role": "student"
  }
}
```

### POST /auth/google
Google OAuth authentication endpoint.

**Request Body:**
```json
{
  "googleIdToken": "google_oauth_token_here"
}
```

**Response:**
```json
{
  "success": true,
  "redirect": "/home"
}
```

### POST /register
User registration endpoint.

**Parameters:**
- `username` (string, required)
- `email` (string, required)
- `password` (string, required)
- `first_name` (string, required)
- `last_name` (string, required)
- `role` (string, required): student|teacher|admin

**Response:**
Redirects to login page on success.

## Course Management Endpoints

### GET /courses
Retrieve courses for the authenticated user.

**Query Parameters:**
- `teacherId` (optional): Filter by teacher ID

**Response:**
Returns JSP page with course list.

### POST /courses
Create a new course (Teacher/Admin only).

**Form Parameters:**
- `name` (string, required)
- `description` (string, required)
- `image` (file, required): Course thumbnail

**Response:**
Redirects to course management page.

### GET /lectures
Retrieve lectures for a specific course.

**Query Parameters:**
- `courseId` (integer, required)

**Response:**
Returns JSP page with lecture list.

### POST /lectures
Create a new lecture (Teacher only).

**Form Parameters:**
- `courseId` (integer, required)
- `title` (string, required)
- `content` (string, required)
- `videoUrl` (string, optional)

## Assignment Management

### GET /assignments
Retrieve assignments for a lecture.

**Query Parameters:**
- `lectureId` (integer, required)

**Response:**
Returns JSP page with assignment list and submissions.

### POST /assignments
Create or manage assignments.

**Form Parameters:**
- `action` (string): create|update|delete
- `courseId` (integer, required)
- `title` (string, required for create/update)
- `description` (string, required for create/update)
- `dueDate` (datetime, required for create/update)

## File Upload Endpoints

### POST /submit
Submit assignment files.

**Form Parameters:**
- `assignmentId` (integer, required)
- `studentId` (integer, required)
- `file` (multipart file, required)

**File Constraints:**
- Maximum size: 10MB
- Supported formats: PDF, DOC, DOCX, TXT, ZIP

**Response:**
Redirects to assignment page with success message.

## Admin Endpoints

### GET /admin/dashboard
Admin dashboard with user management.

**Access:** Admin only

**Response:**
Returns admin dashboard JSP with user statistics and management interface.

### POST /admin/dashboard
Admin user management actions.

**Form Parameters:**
- `action` (string): deleteUser|updateUserRole
- `userId` (integer, required)
- `newRole` (string, required for updateUserRole): student|teacher|admin

### GET /admin/system
System monitoring dashboard.

**Access:** Admin only

**Response:**
Returns system monitoring JSP with:
- Memory usage statistics
- User count by role
- System information
- Server uptime

### POST /admin/system
System maintenance actions.

**Form Parameters:**
- `action` (string): clearCache|gc|exportLogs

**Actions:**
- `clearCache`: Clear application cache
- `gc`: Request garbage collection
- `exportLogs`: Download system logs

### GET /admin/reports
Administrative reports and analytics.

**Query Parameters:**
- `type` (string, optional): overview|users|activity

**Access:** Admin only

**Response:**
Returns reports JSP with detailed analytics.

### GET /admin/reports?type=export
Export reports in CSV format.

**Query Parameters:**
- `reportType` (string): overview|users|activity
- `format` (string): csv

**Response:**
Downloads CSV file with report data.

## User Management

### GET /profile
User profile management page.

**Response:**
Returns profile JSP with user information and activity data.

### POST /profile
Update user profile information.

**Form Parameters:**
- `firstName` (string)
- `lastName` (string)
- `phone` (string)
- `address` (string)
- `school` (string)
- `gender` (string)

### GET /students
Student management interface (Teacher/Admin).

**Query Parameters:**
- `courseId` (integer, optional): Filter students by course

**Response:**
Returns student list JSP.

### POST /students
Manage student enrollment.

**Form Parameters:**
- `action` (string): add|remove
- `courseId` (integer, required)
- `studentId` (integer, required)

## Error Handling

### Error Response Format
All endpoints return appropriate HTTP status codes:

- `200 OK`: Successful request
- `400 Bad Request`: Invalid parameters
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

### Error Pages
- `/error?code=403`: Access denied page
- `/error?code=404`: Page not found
- `/error?code=500`: Server error page

## Security Headers

All responses include security headers:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`

## Rate Limiting

Currently no rate limiting implemented. Recommended for production:
- Authentication endpoints: 5 requests per minute
- File upload endpoints: 10 requests per hour
- Admin endpoints: 100 requests per hour

## Content Types

### Accepted Content Types
- `application/x-www-form-urlencoded`: Form submissions
- `multipart/form-data`: File uploads
- `application/json`: API requests (Google OAuth)

### Response Content Types
- `text/html`: JSP page responses
- `application/json`: AJAX responses
- `text/csv`: Report exports
- `text/plain`: Log exports

---

*This API reference covers all available endpoints in the Learning Management System. For implementation details, refer to the source code and technical documentation.*
