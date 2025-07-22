   <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
    <!DOCTYPE html>
    <html lang="en" data-theme="light">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title></title>
        <!-- Bootstrap CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <!-- Bootstrap Icons -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
        <!-- Font Awesome for Book Icon -->
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <!-- Custom CSS -->
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/homePage.css"/>
        <!-- Google Fonts: Inter -->
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    </head>


<!-- Navigation Menu -->
        <nav class="navbar navbar-expand-lg navbar-light">
            <div class="container-fluid">
                <a class="navbar-brand" href="${pageContext.request.contextPath}/home">
                    <img src="${pageContext.request.contextPath}/image/logo1.jpg" alt="Learning Platform" class="logo-img">
                    <span class="brand-text">EduPlatform</span>
                </a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarNav">
                    <ul class="navbar-nav mx-auto">
                       
                        <% String role = (String) session.getAttribute("role"); %>
                        <% String username = (String) session.getAttribute("username"); %>
                        <% if ("teacher".equals(role)) { %>  
                            <li class="nav-item">
                            <a class="nav-link active" href="${pageContext.request.contextPath}/home">Home</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/course">Manage Courses</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/gradeAssignments">Grade Assignments</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/studentList">Student List</a>
                            </li>
                        <% } else if("student".equals(role)) { %>
                             <li class="nav-item">
                            <a class="nav-link active" href="${pageContext.request.contextPath}/home">Home</a>
                            </li> 
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/myClassroom">My Courses</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/browse">Browse Courses</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="#">Practice</a>
                            </li>
                        <% } else if (username != null) { %>
                            <!-- For logged in users without specific roles -->
                            <li class="nav-item">
                                <a class="nav-link active" href="${pageContext.request.contextPath}/home">Home</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/browse">Browse Courses</a>
                            </li>
                        <% } else { %>
                            <!-- For non-logged in users -->
                            <li class="nav-item">
                                <a class="nav-link active" href="${pageContext.request.contextPath}/home">Home</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/browse">Browse Courses</a>
                            </li>
                        <% } %>
                    </ul>
                    <!-- User Profile, Sign In, Sign Up, Logout, and Theme Toggle -->
                    <div class="d-flex align-items-center">
                        <% if (username == null) { %>
                            <a href="${pageContext.request.contextPath}/login" class="btn btn-outline-primary me-2">Sign In</a>
                            <a href="${pageContext.request.contextPath}/register" class="btn btn-outline-success me-2">Sign Up</a>
                        <% } else { %>
                            <div class="user-icon position-relative">
                                <button class="btn btn-outline-primary me-2 dropdown-toggle" style="border: none; background: none;" data-bs-toggle="dropdown" aria-expanded="false">
                                </button>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/profile"><i class="bi bi-person-fill me-2"></i>Profile</a></li>
                                    <li><hr class="dropdown-divider"></li>
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
                                </ul>
                            </div>
                        <% } %>
                        <button id="theme-toggle" class="btn btn-outline-secondary" title="Toggle Theme">
                            <i class="bi bi-sun-fill"></i>
                        </button>
                    </div>
                </div>
            </div>
        </nav>
                        
        <script>
            document.addEventListener('DOMContentLoaded', () => {
                const toggleButton = document.getElementById('theme-toggle');
                const htmlElement = document.documentElement;
                const currentTheme = localStorage.getItem('theme') || 'light';

                // Set initial theme
                htmlElement.setAttribute('data-theme', currentTheme);
                updateToggleIcon(currentTheme);

                // Toggle theme on button click
                toggleButton.addEventListener('click', () => {
                    const newTheme = htmlElement.getAttribute('data-theme') === 'light' ? 'dark' : 'light';
                    htmlElement.setAttribute('data-theme', newTheme);
                    localStorage.setItem('theme', newTheme);
                    updateToggleIcon(newTheme);
                });

                function updateToggleIcon(theme) {
                    const icon = toggleButton.querySelector('i');
                    icon.className = theme === 'light' ? 'bi bi-sun-fill' : 'bi bi-moon-fill';
                }
            });
        </script>