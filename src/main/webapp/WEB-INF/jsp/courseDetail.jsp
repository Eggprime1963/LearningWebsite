<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${course.name} - Course Details</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/homePage.css">
</head>
<body>
    <jsp:include page="navbar.jsp" />
    
    <!-- Success/Error Messages -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show mx-auto mt-3" role="alert" style="max-width: 800px;">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>
            <strong>Error!</strong> ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${param.message == 'enrolled_successfully'}">
        <div class="alert alert-success alert-dismissible fade show mx-auto mt-3" role="alert" style="max-width: 800px;">
            <i class="bi bi-check-circle-fill me-2"></i>
            <strong>Success!</strong> You have been successfully enrolled in this course.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${param.message == 'already_enrolled'}">
        <div class="alert alert-info alert-dismissible fade show mx-auto mt-3" role="alert" style="max-width: 800px;">
            <i class="bi bi-info-circle-fill me-2"></i>
            <strong>Info:</strong> You are already enrolled in this course.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    
    <div class="container mt-5">
        <div class="row">
            <!-- Course Information Section -->
            <div class="col-md-6">
                <h1 class="display-4 fw-bold mb-3">${course.name}</h1>
                
                <div class="mb-4">
                    <h5 class="text-muted">
                        <i class="bi bi-person-fill me-2"></i>
                        Instructor: ${course.teacherName}
                    </h5>
                </div>
                
                <div class="mb-4">
                    <h6 class="fw-bold mb-3">Course Description</h6>
                    <p class="lead text-muted">${course.description}</p>
                </div>
                
                <div class="mb-4">
                    <h6 class="fw-bold mb-3">Course Details</h6>
                    <ul class="list-unstyled">
                        <li class="mb-2">
                            <i class="bi bi-play-circle-fill me-2 text-primary"></i>
                            <strong>Total Lectures:</strong> ${course.lectureCount}
                        </li>
                        <li class="mb-2">
                            <i class="bi bi-clock-fill me-2 text-success"></i>
                            <strong>Duration:</strong> Self-paced
                        </li>
                        <li class="mb-2">
                            <i class="bi bi-award-fill me-2 text-warning"></i>
                            <strong>Certificate:</strong> Available upon completion
                        </li>
                    </ul>
                </div>
                
                <!-- Enrollment Button -->
                <div class="d-grid gap-2">
                    <c:choose>
                        <c:when test="${empty sessionScope.username}">
                            <!-- Not logged in - Sign In to Start Learning -->
                            <a href="${pageContext.request.contextPath}/login" class="btn btn-primary btn-lg">
                                <i class="bi bi-box-arrow-in-right me-2"></i>
                                Sign In to Start Learning
                            </a>
                        </c:when>
                        <c:when test="${enrolled}">
                            <!-- Logged in and enrolled - Continue Learning -->
                            <a href="${pageContext.request.contextPath}/lectures?courseId=${course.idCourse}" class="btn btn-success btn-lg">
                                <i class="bi bi-play-fill me-2"></i>
                                Continue Learning
                            </a>
                        </c:when>
                        <c:otherwise>
                            <!-- Logged in but not enrolled - Enroll Button -->
                            <form action="${pageContext.request.contextPath}/course" method="post" class="d-inline">
                                <input type="hidden" name="action" value="enroll">
                                <input type="hidden" name="courseId" value="${course.idCourse}">
                                <button type="submit" class="btn btn-primary btn-lg w-100">
                                    <i class="bi bi-bookmark-plus-fill me-2"></i>
                                    Enroll Now (Free)
                                </button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            
            <!-- Course Cover Image Section -->
            <div class="col-md-6">
                <div class="card shadow-lg border-0">
                    <div class="position-relative">
                        <c:choose>
                            <c:when test="${not empty course.image}">
                                <img src="${pageContext.request.contextPath}/${course.image}" 
                                     class="card-img-top" 
                                     alt="${course.name}"
                                     style="height: 400px; object-fit: cover;">
                            </c:when>
                            <c:otherwise>
                                <div class="card-img-top d-flex align-items-center justify-content-center bg-light" 
                                     style="height: 400px;">
                                    <div class="text-center text-muted">
                                        <i class="bi bi-image" style="font-size: 4rem;"></i>
                                        <p class="mt-3 fs-5">Course Cover Image</p>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                        
                        <!-- Play button overlay for preview -->
                        <div class="position-absolute top-50 start-50 translate-middle">
                            <button class="btn btn-light btn-lg rounded-circle shadow" 
                                    style="width: 80px; height: 80px; opacity: 0.9;">
                                <i class="bi bi-play-fill fs-2"></i>
                            </button>
                        </div>
                    </div>
                </div>
                
                <!-- Course Stats -->
                <div class="row mt-4">
                    <div class="col-4 text-center">
                        <div class="card border-0 shadow-sm">
                            <div class="card-body">
                                <h5 class="card-title text-primary">${course.lectureCount}</h5>
                                <p class="card-text text-muted small">Lectures</p>
                            </div>
                        </div>
                    </div>
                    <div class="col-4 text-center">
                        <div class="card border-0 shadow-sm">
                            <div class="card-body">
                                <h5 class="card-title text-success">∞</h5>
                                <p class="card-text text-muted small">Lifetime Access</p>
                            </div>
                        </div>
                    </div>
                    <div class="col-4 text-center">
                        <div class="card border-0 shadow-sm">
                            <div class="card-body">
                                <h5 class="card-title text-warning">★ 4.8</h5>
                                <p class="card-text text-muted small">Rating</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- Course Curriculum Section -->
        <div class="row mt-5">
            <div class="col-12">
                <h3 class="fw-bold mb-4">Course Curriculum</h3>
                <div class="card border-0 shadow-sm">
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${not empty lectures}">
                                <div class="accordion" id="curriculumAccordion">
                                    <c:forEach var="lecture" items="${lectures}" varStatus="status">
                                        <div class="accordion-item">
                                            <h2 class="accordion-header" id="heading${status.index}">
                                                <button class="accordion-button collapsed" type="button" 
                                                        data-bs-toggle="collapse" 
                                                        data-bs-target="#collapse${status.index}" 
                                                        aria-expanded="false" 
                                                        aria-controls="collapse${status.index}">
                                                    <i class="bi bi-play-circle me-3"></i>
                                                    ${lecture.title}
                                                    <span class="badge bg-primary ms-auto me-3">Lecture ${status.index + 1}</span>
                                                </button>
                                            </h2>
                                            <div id="collapse${status.index}" 
                                                 class="accordion-collapse collapse" 
                                                 aria-labelledby="heading${status.index}" 
                                                 data-bs-parent="#curriculumAccordion">
                                                <div class="accordion-body">
                                                    <p class="text-muted">${lecture.content}</p>
                                                    <small class="text-success">
                                                        <i class="bi bi-unlock-fill me-1"></i>
                                                        Available after enrollment
                                                    </small>
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="text-center text-muted py-5">
                                    <i class="bi bi-book" style="font-size: 3rem;"></i>
                                    <p class="mt-3">Course curriculum will be available soon.</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>