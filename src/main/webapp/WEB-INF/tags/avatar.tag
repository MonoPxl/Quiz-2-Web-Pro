<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="user" type="id.myitsbroadcast.model.User" required="true" %>
<%@ attribute name="size" type="java.lang.String" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="avatarSize" value="${empty size ? 'md' : size}"/>
<c:choose>
    <c:when test="${not empty user.avatarPath}">
        <img class="avatar avatar-${avatarSize}"
             src="${pageContext.request.contextPath}/uploads/${user.avatarPath}"
             alt=""
             loading="lazy">
    </c:when>
    <c:otherwise>
        <span class="avatar avatar-${avatarSize}" aria-hidden="true"><c:out value="${user.initials}"/></span>
    </c:otherwise>
</c:choose>
