<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/partials/head.jsp"/>

<section class="shell compose-section">
    <c:choose>
        <c:when test="${isEdit}">
            <c:url var="backUrl" value="/posts/${post.slug}"/>
        </c:when>
        <c:otherwise>
            <c:url var="backUrl" value="/dashboard"/>
        </c:otherwise>
    </c:choose>

    <a class="back-link" href="${backUrl}">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" class="icon-xs" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 19.5 8.25 12l7.5-7.5"/>
        </svg>
        ${isEdit ? 'Back to the broadcast' : 'Back to my broadcast'}
    </a>

    <div class="compose-heading">
        <p class="eyebrow"><span class="eyebrow-dot" aria-hidden="true"></span>${isEdit ? 'Edit broadcast' : 'New broadcast'}</p>
        <h1 class="section-title">${isEdit ? 'Refine your broadcast' : 'Share something with campus'}</h1>
        <p class="section-lede">
            ${isEdit
                    ? 'Update the details below. Changing the title refreshes the web address so links stay readable.'
                    : 'Write a clear headline, choose the right category, and decide whether this is an announcement or an event.'}
        </p>
    </div>

    <c:if test="${not empty errors}">
        <div class="notice notice-error" role="alert">
            <strong>Please check the highlighted fields.</strong> Your work has been kept so you can correct it.
        </div>
    </c:if>

    <form class="compose-form" method="post" enctype="multipart/form-data"
          action="${isEdit ? ctx.concat('/posts/').concat(post.slug) : ctx.concat('/posts/create')}?_csrf=${csrfToken}">
        <input type="hidden" name="_csrf" value="${csrfToken}">
        <c:if test="${isEdit}">
            <input type="hidden" name="_method" value="PUT">
        </c:if>

        <div class="compose-main">
            <div class="field">
                <label for="title">Title</label>
                <input id="title" name="title" type="text" maxlength="180" required
                       value="<c:out value='${form.title}'/>"
                       class="${not empty errors.title ? 'is-invalid' : ''}"
                       placeholder="A clear, specific headline">
                <c:if test="${not empty errors.title}">
                    <p class="field-error"><c:out value="${errors.title}"/></p>
                </c:if>
            </div>

            <div class="field-row">
                <div class="field">
                    <label for="category_id">Category</label>
                    <select id="category_id" name="category_id" required
                            class="select ${not empty errors.categoryId ? 'is-invalid' : ''}">
                        <option value="">Choose a category</option>
                        <c:forEach items="${categories}" var="category">
                            <option value="${category.id}" ${form.categoryId == category.id ? 'selected' : ''}>
                                <c:out value="${category.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                    <c:if test="${not empty errors.categoryId}">
                        <p class="field-error"><c:out value="${errors.categoryId}"/></p>
                    </c:if>
                </div>

                <fieldset class="field">
                    <legend>Type</legend>
                    <div class="segmented">
                        <label class="segment">
                            <input type="radio" name="type" value="announcement" ${form.type == 'announcement' ? 'checked' : ''}>
                            <span>Announcement</span>
                        </label>
                        <label class="segment">
                            <input type="radio" name="type" value="event" ${form.type == 'event' ? 'checked' : ''}>
                            <span>Event</span>
                        </label>
                    </div>
                    <c:if test="${not empty errors.type}">
                        <p class="field-error"><c:out value="${errors.type}"/></p>
                    </c:if>
                </fieldset>
            </div>

            <div class="field">
                <label for="body">Body</label>
                <textarea id="body" name="body" rows="14" maxlength="20000" required
                          class="${not empty errors.body ? 'is-invalid' : ''}"
                          placeholder="What should campus know? Add the details that help people act on it."><c:out value="${form.body}"/></textarea>
                <div class="field-footer">
                    <c:if test="${not empty errors.body}">
                        <p class="field-error"><c:out value="${errors.body}"/></p>
                    </c:if>
                    <span class="field-hint">Plain text. Line breaks are preserved.</span>
                </div>
            </div>

            <fieldset class="event-fields" data-event-fields ${form.event ? '' : 'hidden'}>
                <legend>
                    <span class="aside-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" class="icon-sm" aria-hidden="true">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3.75 8.25h16.5M4.5 5.25h15A.75.75 0 0 1 20.25 6v13.5a.75.75 0 0 1-.75.75h-15a.75.75 0 0 1-.75-.75V6a.75.75 0 0 1 .75-.75Z"/>
                        </svg>
                    </span>
                    Event details
                </legend>

                <div class="field-row">
                    <div class="field">
                        <label for="event_starts_at">Starts</label>
                        <input id="event_starts_at" name="event_starts_at" type="datetime-local"
                               value="<c:out value='${form.eventStartsAt}'/>"
                               class="${not empty errors.eventStartsAt ? 'is-invalid' : ''}">
                        <c:if test="${not empty errors.eventStartsAt}">
                            <p class="field-error"><c:out value="${errors.eventStartsAt}"/></p>
                        </c:if>
                    </div>

                    <div class="field">
                        <label for="event_ends_at">Ends</label>
                        <input id="event_ends_at" name="event_ends_at" type="datetime-local"
                               value="<c:out value='${form.eventEndsAt}'/>"
                               class="${not empty errors.eventEndsAt ? 'is-invalid' : ''}">
                        <c:if test="${not empty errors.eventEndsAt}">
                            <p class="field-error"><c:out value="${errors.eventEndsAt}"/></p>
                        </c:if>
                    </div>
                </div>

                <div class="field">
                    <label for="event_location">Location</label>
                    <input id="event_location" name="event_location" type="text" maxlength="255"
                           value="<c:out value='${form.eventLocation}'/>"
                           class="${not empty errors.eventLocation ? 'is-invalid' : ''}"
                           placeholder="Building, room or online link">
                    <c:if test="${not empty errors.eventLocation}">
                        <p class="field-error"><c:out value="${errors.eventLocation}"/></p>
                    </c:if>
                </div>

                <p class="field-hint">Events are informational listings. Attendance is arranged by the organiser.</p>
            </fieldset>
        </div>

        <aside class="compose-side">
            <div class="compose-card">
                <h2 class="aside-heading">Cover image</h2>

                <div class="cover-preview ${empty post.coverImagePath ? 'is-empty' : ''}" data-cover-preview>
                    <c:choose>
                        <c:when test="${not empty post.coverImagePath}">
                            <img src="${ctx}/uploads/${post.coverImagePath}" alt="Current cover image" data-cover-image>
                        </c:when>
                        <c:otherwise>
                            <span class="cover-preview-empty">No cover yet</span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="field">
                    <label for="cover_image" class="sr-only">Cover image</label>
                    <input id="cover_image" name="cover_image" type="file"
                           accept="image/jpeg,image/png,image/webp"
                           data-cover-input
                           class="${not empty errors.coverImage ? 'is-invalid' : ''}">
                    <p class="field-hint">JPG, PNG or WebP, up to 2 MB.</p>
                    <c:if test="${not empty errors.coverImage}">
                        <p class="field-error"><c:out value="${errors.coverImage}"/></p>
                    </c:if>
                </div>

                <c:if test="${not empty post.coverImagePath}">
                    <label class="check">
                        <input type="checkbox" name="remove_cover_image" value="1">
                        <span>Remove the current cover image</span>
                    </label>
                </c:if>
            </div>

            <div class="compose-card">
                <h2 class="aside-heading">Visibility</h2>

                <label class="choice ${form.status == 'draft' ? 'is-selected' : ''}">
                    <input type="radio" name="status" value="draft" ${form.status == 'draft' ? 'checked' : ''}>
                    <span>
                        <strong>Save as draft</strong>
                        <small>Only you can see it. Publish when it is ready.</small>
                    </span>
                </label>

                <label class="choice ${form.status == 'published' ? 'is-selected' : ''}">
                    <input type="radio" name="status" value="published" ${form.status == 'published' ? 'checked' : ''}>
                    <span>
                        <strong>Publish now</strong>
                        <small>Appears in the campus feed immediately.</small>
                    </span>
                </label>

                <c:if test="${not empty errors.status}">
                    <p class="field-error"><c:out value="${errors.status}"/></p>
                </c:if>
            </div>

            <div class="compose-submit">
                <button class="btn btn-primary btn-block" type="submit">
                    ${isEdit ? 'Save changes' : 'Create broadcast'}
                </button>
                <a class="btn btn-quiet btn-block" href="${backUrl}">Cancel</a>
            </div>
        </aside>
    </form>
</section>

<jsp:include page="/WEB-INF/views/partials/foot.jsp"/>
