<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="partials/head.jsp"/>

<section class="shell dashboard-section">
    <div class="dashboard-heading">
        <div>
            <p class="eyebrow"><span class="eyebrow-dot" aria-hidden="true"></span>My broadcast</p>
            <h1 class="section-title">Hello, <c:out value="${currentUser.firstName}"/>.</h1>
            <p class="section-lede">Everything you have shared with campus, and everything you have saved for later.</p>
        </div>

        <a class="btn btn-primary" href="${ctx}/posts/create">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="icon-xs" aria-hidden="true">
                <path stroke-linecap="round" d="M12 5v14M5 12h14"/>
            </svg>
            New broadcast
        </a>
    </div>

    <div class="stat-grid">
        <div class="stat-card stat-card-primary">
            <span class="stat-label">Broadcasts</span>
            <strong class="stat-value"><c:out value="${dashboard.postCount}"/></strong>
            <span class="stat-foot">Everything you have written</span>
        </div>
        <div class="stat-card">
            <span class="stat-label">Published</span>
            <strong class="stat-value"><c:out value="${dashboard.publishedCount}"/></strong>
            <span class="stat-foot">Visible in the campus feed</span>
        </div>
        <div class="stat-card">
            <span class="stat-label">Drafts</span>
            <strong class="stat-value"><c:out value="${dashboard.draftCount}"/></strong>
            <span class="stat-foot">Waiting for the right moment</span>
        </div>
        <div class="stat-card">
            <span class="stat-label">Bookmarks</span>
            <strong class="stat-value"><c:out value="${dashboard.bookmarkCount}"/></strong>
            <span class="stat-foot">Saved from campus</span>
        </div>
    </div>

    <div class="dashboard-grid">
        <section class="panel" aria-labelledby="my-posts-title">
            <div class="panel-heading">
                <h2 id="my-posts-title">Your broadcasts</h2>
                <a class="text-link" href="${ctx}/posts/create">Write a new one</a>
            </div>

            <c:choose>
                <c:when test="${empty dashboard.posts}">
                    <div class="empty-state empty-state-compact">
                        <h3>Nothing published yet</h3>
                        <p>Share an announcement or an event with the campus community.</p>
                        <a class="btn btn-primary" href="${ctx}/posts/create">Create your first broadcast</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <ul class="manage-list">
                        <c:forEach items="${dashboard.posts}" var="post">
                            <li class="manage-item">
                                <div class="manage-thumb ${empty post.coverImagePath ? 'is-empty' : ''}">
                                    <c:if test="${not empty post.coverImagePath}">
                                        <img src="${ctx}/uploads/${post.coverImagePath}" alt="" loading="lazy">
                                    </c:if>
                                </div>

                                <div class="manage-copy">
                                    <div class="pill-row">
                                        <span class="pill ${post.event ? 'pill-event' : ''}"><c:out value="${post.type.label}"/></span>
                                        <span class="pill pill-status-${post.status.value}"><c:out value="${post.status.label}"/></span>
                                    </div>

                                    <h3 class="manage-title">
                                        <a href="${ctx}/posts/${post.slug}"><c:out value="${post.title}"/></a>
                                    </h3>

                                    <p class="manage-meta">
                                        Updated <c:out value="${dates.date(post.updatedAt)}"/>
                                        &middot; <c:out value="${post.commentCount}"/> comments
                                        &middot; <c:out value="${post.bookmarkCount}"/> bookmarks
                                    </p>
                                </div>

                                <div class="manage-actions">
                                    <a class="btn btn-quiet btn-sm" href="${ctx}/posts/${post.slug}/edit">Edit</a>

                                    <form method="post" action="${ctx}/posts/${post.slug}"
                                          data-confirm="Delete this broadcast? This cannot be undone.">
                                        <input type="hidden" name="_csrf" value="${csrfToken}">
                                        <input type="hidden" name="_method" value="DELETE">
                                        <button class="btn btn-danger btn-sm" type="submit">Delete</button>
                                    </form>
                                </div>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </section>

        <aside class="dashboard-aside">
            <section class="panel" aria-labelledby="saved-title">
                <div class="panel-heading">
                    <h2 id="saved-title">Saved broadcasts</h2>
                </div>

                <c:choose>
                    <c:when test="${empty dashboard.bookmarks}">
                        <div class="empty-state empty-state-compact">
                            <h3>No bookmarks yet</h3>
                            <p>Save broadcasts from the feed to find them here.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <ul class="saved-list">
                            <c:forEach items="${dashboard.bookmarks}" var="bookmark">
                                <li class="saved-item">
                                    <a href="${ctx}/posts/${bookmark.post.slug}">
                                        <span class="saved-category"><c:out value="${bookmark.post.category.name}"/></span>
                                        <span class="saved-title"><c:out value="${bookmark.post.title}"/></span>
                                        <span class="saved-meta">Saved <c:out value="${dates.date(bookmark.createdAt)}"/></span>
                                    </a>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="panel panel-quiet" aria-labelledby="account-title">
                <div class="panel-heading">
                    <h2 id="account-title">Account</h2>
                </div>
                <p class="panel-text">Manage your profile, photo and password. Sign-out controls live in profile settings.</p>

                <div class="account-links">
                    <a class="text-link" href="${ctx}/settings/profile">Profile settings</a>
                    <a class="text-link" href="${ctx}/settings/security">Password</a>
                </div>
            </section>
        </aside>
    </div>
</section>

<jsp:include page="partials/foot.jsp"/>
