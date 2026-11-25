<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="site-nav" aria-label="Primary navigation">
    <a href="${ctx}/"
       class="site-nav-link ${currentNav == 'feed' ? 'is-active' : ''}"
       ${currentNav == 'feed' ? 'aria-current="page"' : ''}>Feed</a>

    <c:if test="${not empty currentUser}">
        <a href="${ctx}/dashboard"
           class="site-nav-link ${currentNav == 'dashboard' ? 'is-active' : ''}"
           ${currentNav == 'dashboard' ? 'aria-current="page"' : ''}>My Broadcast</a>
    </c:if>
</nav>
