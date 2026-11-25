<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="partials/head.jsp"/>

<section class="shell feed-section">
    <div class="feed-masthead">
        <div class="feed-intro">
            <p class="eyebrow"><span class="eyebrow-dot" aria-hidden="true"></span>Campus bulletin</p>
            <h2 class="feed-title">Latest from across <em>ITS</em>.</h2>
            <p class="feed-lede">Announcements, events and opportunities, published by the campus community and gathered in one calm, readable place.</p>

            <c:if test="${not empty currentUser}">
                <a class="btn btn-primary" href="${ctx}/posts/create">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="icon-xs" aria-hidden="true">
                        <path stroke-linecap="round" d="M12 5v14M5 12h14"/>
                    </svg>
                    New broadcast
                </a>
            </c:if>
        </div>

        <aside class="signal-card" aria-label="About the campus feed">
            <span class="signal-card-kicker">The campus signal</span>
            <strong>One place for every update that matters.</strong>
            <span class="signal-card-meta">Written by students, staff and campus organisations.</span>
            <svg class="signal-card-wave" viewBox="0 0 320 60" fill="none" aria-hidden="true">
                <path d="M0 32h44c16 0 17-15 33-15s17 22 34 22 17-22 34-22 17 22 34 22 17-22 34-22 17 15 33 15h74" stroke="#91BAFF" stroke-width="1.6"/>
                <path d="M0 44h38c15 0 17-12 32-12s17 14 34 14 17-14 34-14 17 14 34 14 17-14 34-14 17 12 32 12h82" stroke="#00AABA" stroke-width="1" opacity=".75"/>
            </svg>
        </aside>
    </div>

    <form class="filter-bar" method="get" action="${ctx}/" role="search">
        <div class="search-field">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="icon-sm" aria-hidden="true">
                <path stroke-linecap="round" stroke-linejoin="round" d="m21 21-4.35-4.35M17 11a6 6 0 1 1-12 0 6 6 0 0 1 12 0Z"/>
            </svg>
            <label class="sr-only" for="q">Search broadcasts</label>
            <input id="q" name="q" type="search" value="${fn:escapeXml(q)}"
                   placeholder="Search announcements, events…" autocomplete="off">
        </div>

        <div class="filter-controls">
            <label class="sr-only" for="category">Category</label>
            <select id="category" name="category" class="select">
                <option value="">All categories</option>
                <c:forEach items="${categories}" var="category">
                    <option value="${category.slug}" ${selectedCategory == category.slug ? 'selected' : ''}>
                        <c:out value="${category.name}"/>
                    </option>
                </c:forEach>
            </select>

            <label class="sr-only" for="type">Type</label>
            <select id="type" name="type" class="select">
                <option value="">All types</option>
                <option value="announcement" ${selectedType == 'announcement' ? 'selected' : ''}>Announcements</option>
                <option value="event" ${selectedType == 'event' ? 'selected' : ''}>Events</option>
            </select>

            <button class="btn btn-primary" type="submit">Filter</button>

            <c:if test="${hasFilters}">
                <a class="btn btn-quiet" href="${ctx}/">Reset</a>
            </c:if>
        </div>
    </form>

    <div class="feed-toolbar">
        <div>
            <h2 class="feed-list-title">${hasFilters ? 'Search results' : 'Latest broadcasts'}</h2>
            <p class="feed-list-meta">
                <c:choose>
                    <c:when test="${posts.total == 1}">1 broadcast</c:when>
                    <c:otherwise><c:out value="${posts.total}"/> broadcasts</c:otherwise>
                </c:choose>
                <c:if test="${hasFilters}"> matching the current filters</c:if>
            </p>
        </div>

        <c:if test="${hasFilters}">
            <a class="text-link" href="${ctx}/">Clear all filters</a>
        </c:if>
    </div>

    <c:choose>
        <c:when test="${empty posts.items}">
            <div class="empty-state">
                <span class="empty-mark" aria-hidden="true">
                    <svg viewBox="0 0 40 40" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                        <circle cx="8" cy="20" r="2" fill="currentColor" stroke="none"/>
                        <path d="M14 13a10 10 0 0 1 0 14M19 8a17 17 0 0 1 0 24M24 3a24 24 0 0 1 0 34"/>
                    </svg>
                </span>
                <h3>${hasFilters ? 'No broadcasts match those filters' : 'The feed is quiet for now'}</h3>
                <p>${hasFilters
                        ? 'Try a different keyword, category or type.'
                        : 'Published announcements and events will appear here.'}</p>
                <c:if test="${hasFilters}">
                    <a class="btn btn-primary" href="${ctx}/">Clear filters</a>
                </c:if>
            </div>
        </c:when>
        <c:otherwise>
            <div class="post-grid">
                <c:forEach items="${posts.items}" var="post" varStatus="loop">
                    <c:choose>
                        <c:when test="${loop.first and posts.page == 1 and not hasFilters}">
                            <ui:postCard post="${post}" featured="true"/>
                        </c:when>
                        <c:otherwise>
                            <ui:postCard post="${post}"/>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </div>

            <ui:pagination page="${posts}" q="${q}" category="${selectedCategory}" type="${selectedType}"/>
        </c:otherwise>
    </c:choose>
</section>

<jsp:include page="partials/foot.jsp"/>
