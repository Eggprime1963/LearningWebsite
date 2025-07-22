<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en" data-theme="light">
<head>
    <meta charset="UTF-8">
    <title>Browse Courses</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/homePage.css"/>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
</head>
<body>
    <jsp:include page="navbar.jsp" />
    
    <!-- Error Messages -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show mx-auto mt-3" role="alert" style="max-width: 1200px;">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>
            <strong>Error!</strong> ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    
    <div class="container mt-4">
        <div class="text-center mb-5">
            <h1 class="display-4 fw-bold">Browse Courses</h1>
            <p class="lead text-muted">Discover amazing courses and start your learning journey</p>
        </div>
        
        <div class="row">
            <c:forEach var="course" items="${courses}">
                <div class="col-md-4 mb-4">
                    <div class="card h-100 shadow-sm border-0 course-card">
                        <c:choose>
                            <c:when test="${not empty course.image}">
                                <img src="${pageContext.request.contextPath}/${course.image}" 
                                     class="card-img-top" 
                                     alt="${course.name}"
                                     style="height: 200px; object-fit: cover;">
                            </c:when>
                            <c:otherwise>
                                <div class="card-img-top d-flex align-items-center justify-content-center bg-light" 
                                     style="height: 200px;">
                                    <i class="bi bi-book text-muted" style="font-size: 3rem;"></i>
                                </div>
                            </c:otherwise>
                        </c:choose>
                        
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title fw-bold">${course.name}</h5>
                            <p class="card-text text-muted flex-grow-1">${course.description}</p>
                            
                            <div class="mt-auto">
                                <div class="d-flex justify-content-between align-items-center mb-3">
                                    <small class="text-muted">
                                        <i class="bi bi-play-circle me-1"></i>
                                        ${course.lectureCount} lectures
                                    </small>
                                    <small class="text-success fw-bold">Free</small>
                                </div>
                                
                                <div class="d-grid">
                                    <a href="${pageContext.request.contextPath}/course?courseId=${course.idCourse}" 
                                       class="btn btn-primary">
                                        <i class="bi bi-eye me-2"></i>View Course
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
            
            <c:if test="${empty courses}">
                <div class="col-12">
                    <div class="text-center py-5">
                        <i class="bi bi-book text-muted" style="font-size: 4rem;"></i>
                        <h3 class="mt-3 text-muted">No courses available</h3>
                        <p class="text-muted">Check back later for new courses!</p>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
