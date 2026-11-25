<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="post" type="id.myitsbroadcast.model.Post" required="true" %>
<%@ attribute name="featured" type="java.lang.Boolean" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<article class="post-card${featured ? ' post-card-featured' : ''}">
    <a class="post-card-media ${post.event ? 'is-event' : ''}"
       href="${pageContext.request.contextPath}/posts/${post.slug}"
       aria-hidden="true"
       tabindex="-1">
        <c:choose>
            <c:when test="${not empty post.coverImagePath}">
                <img src="${pageContext.request.contextPath}/uploads/${post.coverImagePath}"
                     alt=""
                     loading="lazy">
            </c:when>
            <c:otherwise>
                <span class="post-card-placeholder">
                    <span class="placeholder-signal" aria-hidden="true">
                        <svg viewBox="0 0 40 40" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
                            <circle cx="8" cy="20" r="2" fill="currentColor" stroke="none"/>
                            <path d="M14 13a10 10 0 0 1 0 14M19 8a17 17 0 0 1 0 24M24 3a24 24 0 0 1 0 34"/>
                        </svg>
                    </span>
                    <span class="media-label"><c:out value="${post.type.label}"/></span>
                </span>
            </c:otherwise>
        </c:choose>
    </a>

    <div class="post-card-body">
        <div class="pill-row">
            <span class="pill ${post.event ? 'pill-event' : ''}"><c:out value="${post.type.label}"/></span>
            <c:if test="${post.draft}">
                <span class="pill pill-draft">Draft</span>
            </c:if>
        </div>

        <h3 class="post-card-title">
            <a href="${pageContext.request.contextPath}/posts/${post.slug}"><c:out value="${post.title}"/></a>
        </h3>

        <p class="post-card-excerpt"><c:out value="${post.excerpt}"/></p>
    </div>

    <div class="post-card-meta">
        <span class="meta-category"><c:out value="${post.category.name}"/></span>
        <span class="meta-sep" aria-hidden="true"></span>
        <span><c:out value="${dates.date(post.publishedAt)}"/></span>
        <span class="meta-end">
            <span class="meta-stat" aria-label="${post.commentCount} comments">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true" class="icon-xs">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M8.625 12a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm3.75 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm3.75 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0ZM21 12c0 4.556-4.03 8.25-9 8.25a9.76 9.76 0 0 1-2.555-.337A5.972 5.972 0 0 1 5.41 20.97a5.969 5.969 0 0 1-.474-.065 4.48 4.48 0 0 0 .978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25Z"/>
                </svg>
                <c:out value="${post.commentCount}"/>
            </span>
            <span class="meta-stat ${post.bookmarked ? 'is-active' : ''}"
                  data-bookmark-stat="${post.id}"
                  aria-label="${post.bookmarked ? 'Bookmarked' : 'Not bookmarked'}">
                <svg viewBox="0 0 24 24" fill="${post.bookmarked ? 'currentColor' : 'none'}" stroke="currentColor" stroke-width="1.7" aria-hidden="true" class="icon-xs">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M17.593 3.322c.1.128.157.286.157.45v17.25L12 17.5l-5.75 3.522V3.772c0-.164.057-.322.157-.45C6.7 2.937 7.6 2.5 8.5 2.5h7c.9 0 1.8.437 2.093.822Z"/>
                </svg>
                <c:out value="${post.bookmarkCount}"/>
            </span>
        </span>
    </div>
</article>
