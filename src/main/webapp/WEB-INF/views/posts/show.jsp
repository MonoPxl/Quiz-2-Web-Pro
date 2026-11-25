<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/partials/head.jsp"/>

<section class="shell detail-section">
    <a class="back-link" href="${ctx}/">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" class="icon-xs" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5"/>
        </svg>
        Back to the feed
    </a>

    <c:if test="${not post.published}">
        <div class="notice notice-warning">
            <strong>This broadcast is a draft.</strong> Only you can see this preview. Publish it from the edit page when it is ready.
        </div>
    </c:if>

    <div class="detail-grid">
        <article class="detail-article">
            <header class="detail-hero ${empty post.coverImagePath ? 'is-plain' : ''}">
                <c:if test="${not empty post.coverImagePath}">
                    <img class="detail-cover" src="${ctx}/uploads/${post.coverImagePath}" alt="">
                </c:if>
                <div class="detail-hero-shade" aria-hidden="true"></div>

                <div class="detail-hero-inner">
                    <div class="pill-row">
                        <span class="pill pill-on-dark"><c:out value="${post.type.label}"/></span>
                        <span class="pill pill-outline-light"><c:out value="${post.category.name}"/></span>
                        <c:if test="${not post.published}">
                            <span class="pill pill-draft"><c:out value="${post.status.label}"/></span>
                        </c:if>
                        <c:if test="${post.event}">
                            <span class="pill pill-event">Event</span>
                        </c:if>
                    </div>

                    <h1 class="detail-title"><c:out value="${post.title}"/></h1>

                    <div class="detail-byline">
                        <span class="byline-author">
                            <ui:avatar user="${post.author}" size="sm"/>
                            <c:out value="${post.author.name}"/>
                        </span>
                        <span class="byline-sep" aria-hidden="true"></span>
                        <span>
                            <c:choose>
                                <c:when test="${post.published}">Published <c:out value="${dates.date(post.publishedAt)}"/></c:when>
                                <c:otherwise>Last updated <c:out value="${dates.date(post.updatedAt)}"/></c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </div>
            </header>

            <div class="detail-body">
                <c:forTokens items="${post.body}" delims="&#10;&#13;" var="paragraph">
                    <c:if test="${not empty fn:trim(paragraph)}">
                        <p><c:out value="${fn:trim(paragraph)}"/></p>
                    </c:if>
                </c:forTokens>
            </div>
        </article>

        <aside class="detail-aside">
            <c:if test="${post.event}">
                <div class="aside-card">
                    <h2 class="aside-heading">
                        <span class="aside-icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="icon-sm" aria-hidden="true">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3.75 8.25h16.5M4.5 5.25h15A.75.75 0 0 1 20.25 6v13.5a.75.75 0 0 1-.75.75h-15a.75.75 0 0 1-.75-.75V6a.75.75 0 0 1 .75-.75Z"/>
                            </svg>
                        </span>
                        Event details
                    </h2>

                    <dl class="event-facts">
                        <c:if test="${not empty post.eventStartsAt}">
                            <div>
                                <dt>Starts</dt>
                                <dd><c:out value="${dates.longDate(post.eventStartsAt)}"/>, <c:out value="${dates.time(post.eventStartsAt)}"/></dd>
                            </div>
                        </c:if>
                        <c:if test="${not empty post.eventEndsAt}">
                            <div>
                                <dt>Ends</dt>
                                <dd><c:out value="${dates.longDate(post.eventEndsAt)}"/>, <c:out value="${dates.time(post.eventEndsAt)}"/></dd>
                            </div>
                        </c:if>
                        <c:if test="${not empty post.eventLocation}">
                            <div>
                                <dt>Location</dt>
                                <dd><c:out value="${post.eventLocation}"/></dd>
                            </div>
                        </c:if>
                    </dl>

                    <p class="event-note">This listing is informational. Check with the organiser for attendance details.</p>
                </div>
            </c:if>

            <div class="aside-card">
                <h2 class="aside-heading">Author</h2>

                <div class="author-block">
                    <ui:avatar user="${post.author}" size="lg"/>
                    <div class="author-copy">
                        <p class="author-name"><c:out value="${post.author.name}"/></p>
                        <p class="author-email"><c:out value="${post.author.email}"/></p>
                    </div>
                </div>

                <div class="aside-stats">
                    <div>
                        <span class="stat-label">Comments</span>
                        <strong id="comment-total"><c:out value="${commentCount}"/></strong>
                    </div>
                    <div>
                        <span class="stat-label">Bookmarks</span>
                        <strong data-bookmark-count><c:out value="${post.bookmarkCount}"/></strong>
                    </div>
                </div>

                <c:if test="${post.published}">
                    <div class="aside-actions">
                        <c:choose>
                            <c:when test="${not empty currentUser}">
                                <form class="bookmark-form" method="post" action="${ctx}/bookmarks"
                                      data-bookmark-form data-post-id="${post.id}">
                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                    <input type="hidden" name="post" value="${post.slug}">
                                    <button type="submit"
                                            class="btn btn-bookmark ${bookmarked ? 'is-active' : ''}"
                                            data-bookmark-button
                                            aria-pressed="${bookmarked}">
                                        <svg viewBox="0 0 24 24" fill="${bookmarked ? 'currentColor' : 'none'}" stroke="currentColor" stroke-width="1.8" class="icon-xs" aria-hidden="true">
                                            <path stroke-linecap="round" stroke-linejoin="round" d="M17.593 3.322c.1.128.157.286.157.45v17.25L12 17.5l-5.75 3.522V3.772c0-.164.057-.322.157-.45C6.7 2.937 7.6 2.5 8.5 2.5h7c.9 0 1.8.437 2.093.822Z"/>
                                        </svg>
                                        <span data-bookmark-label>${bookmarked ? 'Bookmarked' : 'Bookmark'}</span>
                                    </button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <a class="text-link" href="${ctx}/login">Log in to bookmark</a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <c:if test="${canEditPost}">
                    <div class="aside-actions aside-actions-split">
                        <a class="btn btn-quiet" href="${ctx}/posts/${post.slug}/edit">Edit</a>

                        <form method="post" action="${ctx}/posts/${post.slug}"
                              data-confirm="Delete this broadcast? This cannot be undone.">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="_method" value="DELETE">
                            <button type="submit" class="btn btn-danger">Delete</button>
                        </form>
                    </div>
                </c:if>
            </div>
        </aside>
    </div>

    <section class="comments" id="comments" aria-labelledby="comments-title">
        <div class="comments-heading">
            <div>
                <p class="eyebrow"><span class="eyebrow-dot" aria-hidden="true"></span>Discussion</p>
                <h2 id="comments-title" class="section-title">
                    <c:out value="${commentCount}"/>
                    <c:choose>
                        <c:when test="${commentCount == 1}">comment</c:when>
                        <c:otherwise>comments</c:otherwise>
                    </c:choose>
                </h2>
            </div>
        </div>

        <c:choose>
            <c:when test="${not post.published}">
                <div class="empty-state empty-state-compact">
                    <h3>Discussion opens when this broadcast is published</h3>
                    <p>Drafts stay private, so nobody can comment on them yet.</p>
                </div>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${not empty currentUser}">
                        <form class="comment-form" method="post" action="${ctx}/comments">
                            <input type="hidden" name="_csrf" value="${csrfToken}">
                            <input type="hidden" name="post" value="${post.slug}">

                            <div class="comment-form-author">
                                <ui:avatar user="${currentUser}" size="sm"/>
                                <span>Commenting as <strong><c:out value="${currentUser.name}"/></strong></span>
                            </div>

                            <label class="sr-only" for="comment-body">Write a comment</label>
                            <textarea id="comment-body" name="body" rows="4" maxlength="2000"
                                      placeholder="Add to the conversation…"
                                      class="${not empty commentErrors.body ? 'is-invalid' : ''}"><c:out value="${commentBody}"/></textarea>

                            <c:if test="${not empty commentErrors.body}">
                                <p class="field-error"><c:out value="${commentErrors.body}"/></p>
                            </c:if>

                            <div class="form-actions">
                                <span class="field-hint">Plain text, up to 2,000 characters.</span>
                                <button class="btn btn-primary" type="submit">Post comment</button>
                            </div>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <div class="comment-login">
                            <p>Join the discussion to reply to this broadcast.</p>
                            <a class="btn btn-primary" href="${ctx}/login">Log in to comment</a>
                        </div>
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${empty comments}">
                        <div class="empty-state empty-state-compact">
                            <h3>No comments yet</h3>
                            <p>Be the first to respond to this broadcast.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <ol class="comment-list">
                            <c:forEach items="${comments}" var="comment">
                                <li class="comment" id="comment-${comment.id}">
                                    <div class="comment-head">
                                        <ui:avatar user="${comment.user}" size="sm"/>
                                        <div class="comment-meta">
                                            <span class="comment-author"><c:out value="${comment.user.name}"/></span>
                                            <span class="comment-time">
                                                <c:out value="${dates.dateTime(comment.createdAt)}"/>
                                                <c:if test="${comment.edited}"> &middot; edited</c:if>
                                            </span>
                                        </div>
                                    </div>

                                    <c:choose>
                                        <c:when test="${editingCommentId == comment.id}">
                                            <form class="comment-edit" method="post" action="${ctx}/comments/${comment.id}">
                                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                                <input type="hidden" name="_method" value="PUT">
                                                <label class="sr-only" for="edit-comment-${comment.id}">Edit comment</label>
                                                <textarea id="edit-comment-${comment.id}" name="body" rows="3" maxlength="2000"
                                                          class="${not empty commentErrors.body ? 'is-invalid' : ''}"><c:out value="${commentBody}"/></textarea>
                                                <c:if test="${not empty commentErrors.body}">
                                                    <p class="field-error"><c:out value="${commentErrors.body}"/></p>
                                                </c:if>
                                                <div class="form-actions">
                                                    <a class="btn btn-quiet" href="${ctx}/posts/${post.slug}#comment-${comment.id}">Cancel</a>
                                                    <button class="btn btn-primary" type="submit">Save changes</button>
                                                </div>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <p class="comment-body"><c:out value="${comment.body}"/></p>

                                            <div class="comment-actions">
                                                <c:if test="${not empty currentUser}">
                                                    <details class="reply-toggle">
                                                        <summary>Reply</summary>
                                                        <form class="reply-form" method="post" action="${ctx}/comments">
                                                            <input type="hidden" name="_csrf" value="${csrfToken}">
                                                            <input type="hidden" name="post" value="${post.slug}">
                                                            <input type="hidden" name="parent_id" value="${comment.id}">
                                                            <label class="sr-only" for="reply-${comment.id}">Reply to <c:out value="${comment.user.name}"/></label>
                                                            <textarea id="reply-${comment.id}" name="body" rows="3" maxlength="2000"
                                                                      placeholder="Write a reply…"></textarea>
                                                            <div class="form-actions">
                                                                <button class="btn btn-primary btn-sm" type="submit">Post reply</button>
                                                            </div>
                                                        </form>
                                                    </details>
                                                </c:if>

                                                <c:if test="${not empty currentUser and comment.userId == currentUser.id}">
                                                    <a class="comment-action" href="${ctx}/posts/${post.slug}?edit=${comment.id}#comment-${comment.id}"
                                                       data-edit-comment="${comment.id}">Edit</a>

                                                    <form method="post" action="${ctx}/comments/${comment.id}"
                                                          data-confirm="Delete this comment? Replies will be removed too.">
                                                        <input type="hidden" name="_csrf" value="${csrfToken}">
                                                        <input type="hidden" name="_method" value="DELETE">
                                                        <button type="submit" class="comment-action comment-action-danger">Delete</button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>

                                    <c:if test="${not empty comment.replies}">
                                        <ol class="reply-list">
                                            <c:forEach items="${comment.replies}" var="reply">
                                                <li class="comment reply" id="comment-${reply.id}">
                                                    <div class="comment-head">
                                                        <ui:avatar user="${reply.user}" size="xs"/>
                                                        <div class="comment-meta">
                                                            <span class="comment-author"><c:out value="${reply.user.name}"/></span>
                                                            <span class="comment-time"><c:out value="${dates.dateTime(reply.createdAt)}"/></span>
                                                        </div>
                                                    </div>

                                                    <c:choose>
                                                        <c:when test="${editingCommentId == reply.id}">
                                                            <form class="comment-edit" method="post" action="${ctx}/comments/${reply.id}">
                                                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                                                <input type="hidden" name="_method" value="PUT">
                                                                <label class="sr-only" for="edit-comment-${reply.id}">Edit reply</label>
                                                                <textarea id="edit-comment-${reply.id}" name="body" rows="3" maxlength="2000"
                                                                          class="${not empty commentErrors.body ? 'is-invalid' : ''}"><c:out value="${commentBody}"/></textarea>
                                                                <c:if test="${not empty commentErrors.body}">
                                                                    <p class="field-error"><c:out value="${commentErrors.body}"/></p>
                                                                </c:if>
                                                                <div class="form-actions">
                                                                    <a class="btn btn-quiet" href="${ctx}/posts/${post.slug}#comment-${reply.id}">Cancel</a>
                                                                    <button class="btn btn-primary" type="submit">Save changes</button>
                                                                </div>
                                                            </form>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <p class="comment-body"><c:out value="${reply.body}"/></p>

                                                            <div class="comment-actions">
                                                                <c:if test="${not empty currentUser and reply.userId == currentUser.id}">
                                                                    <a class="comment-action" href="${ctx}/posts/${post.slug}?edit=${reply.id}#comment-${reply.id}"
                                                                       data-edit-comment="${reply.id}">Edit</a>

                                                                    <form method="post" action="${ctx}/comments/${reply.id}"
                                                                          data-confirm="Delete this reply?">
                                                                        <input type="hidden" name="_csrf" value="${csrfToken}">
                                                                        <input type="hidden" name="_method" value="DELETE">
                                                                        <button type="submit" class="comment-action comment-action-danger">Delete</button>
                                                                    </form>
                                                                </c:if>
                                                            </div>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </li>
                                            </c:forEach>
                                        </ol>
                                    </c:if>
                                </li>
                            </c:forEach>
                        </ol>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>
    </section>
</section>

<jsp:include page="/WEB-INF/views/partials/foot.jsp"/>
