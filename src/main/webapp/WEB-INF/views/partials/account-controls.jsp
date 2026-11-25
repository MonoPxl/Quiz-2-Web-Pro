<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<c:choose>
    <c:when test="${not empty currentUser}">
        <a class="icon-action ${currentNav == 'create' ? 'is-active' : ''}"
           href="${ctx}/posts/create"
           aria-label="Create a broadcast"
           title="Create a broadcast">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <path stroke-linecap="round" d="M12 5v14M5 12h14"/>
            </svg>
        </a>

        <a class="profile-chip ${currentNav == 'profile' ? 'is-active' : ''}"
           href="${ctx}/settings/profile"
           ${currentNav == 'profile' ? 'aria-current="page"' : ''}>
            <ui:avatar user="${currentUser}" size="sm"/>
            <span class="profile-chip-label">My Profile</span>
        </a>
    </c:when>
    <c:otherwise>
        <a class="btn btn-quiet-light" href="${ctx}/login">Log in</a>
        <a class="btn btn-light" href="${ctx}/register">Register</a>
    </c:otherwise>
</c:choose>
