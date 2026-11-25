<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="page" type="id.myitsbroadcast.model.Page" required="true" %>
<%@ attribute name="q" type="java.lang.String" required="false" %>
<%@ attribute name="category" type="java.lang.String" required="false" %>
<%@ attribute name="type" type="java.lang.String" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${page.totalPages > 1}">
    <nav class="pagination" aria-label="Broadcast pages">
        <c:url var="previousUrl" value="/">
            <c:param name="page" value="${page.previousPage}"/>
            <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
            <c:if test="${not empty category}"><c:param name="category" value="${category}"/></c:if>
            <c:if test="${not empty type}"><c:param name="type" value="${type}"/></c:if>
        </c:url>

        <c:choose>
            <c:when test="${page.hasPrevious}">
                <a class="page-link page-direction" href="${previousUrl}" rel="prev">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="icon-xs" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5"/></svg>
                    Previous
                </a>
            </c:when>
            <c:otherwise>
                <span class="page-link page-direction is-disabled" aria-disabled="true">Previous</span>
            </c:otherwise>
        </c:choose>

        <c:forEach begin="1" end="${page.totalPages}" var="number">
            <c:if test="${number == 1 or number == page.totalPages or (number >= page.page - 1 and number <= page.page + 1)}">
                <c:url var="pageUrl" value="/">
                    <c:param name="page" value="${number}"/>
                    <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
                    <c:if test="${not empty category}"><c:param name="category" value="${category}"/></c:if>
                    <c:if test="${not empty type}"><c:param name="type" value="${type}"/></c:if>
                </c:url>

                <c:choose>
                    <c:when test="${number == page.page}">
                        <span class="page-link is-current" aria-current="page">${number}</span>
                    </c:when>
                    <c:otherwise>
                        <a class="page-link" href="${pageUrl}">${number}</a>
                    </c:otherwise>
                </c:choose>
            </c:if>

            <c:if test="${number == 1 and page.page > 3}">
                <span class="page-gap" aria-hidden="true">…</span>
            </c:if>
            <c:if test="${number == page.page + 1 and page.page < page.totalPages - 2}">
                <span class="page-gap" aria-hidden="true">…</span>
            </c:if>
        </c:forEach>

        <c:url var="nextUrl" value="/">
            <c:param name="page" value="${page.nextPage}"/>
            <c:if test="${not empty q}"><c:param name="q" value="${q}"/></c:if>
            <c:if test="${not empty category}"><c:param name="category" value="${category}"/></c:if>
            <c:if test="${not empty type}"><c:param name="type" value="${type}"/></c:if>
        </c:url>

        <c:choose>
            <c:when test="${page.hasNext}">
                <a class="page-link page-direction" href="${nextUrl}" rel="next">
                    Next
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="icon-xs" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="m8.25 4.5 7.5 7.5-7.5 7.5"/></svg>
                </a>
            </c:when>
            <c:otherwise>
                <span class="page-link page-direction is-disabled" aria-disabled="true">Next</span>
            </c:otherwise>
        </c:choose>
    </nav>
</c:if>
