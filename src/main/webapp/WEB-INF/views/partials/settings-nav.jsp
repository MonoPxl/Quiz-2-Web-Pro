<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="settings-nav" aria-label="Settings">
    <a class="settings-nav-link ${currentPath == '/settings/profile' ? 'is-active' : ''}"
       href="${ctx}/settings/profile"
       ${currentPath == '/settings/profile' ? 'aria-current="page"' : ''}>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-sm" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.5 20.25a7.5 7.5 0 0 1 15 0"/>
        </svg>
        Profile
    </a>
    <a class="settings-nav-link ${currentPath == '/settings/security' ? 'is-active' : ''}"
       href="${ctx}/settings/security"
       ${currentPath == '/settings/security' ? 'aria-current="page"' : ''}>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-sm" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 1 0-9 0v3.75M6.75 10.5h10.5a.75.75 0 0 1 .75.75v8.25a.75.75 0 0 1-.75.75H6.75a.75.75 0 0 1-.75-.75v-8.25a.75.75 0 0 1 .75-.75Z"/>
        </svg>
        Password
    </a>
    <a class="settings-nav-link" href="${ctx}/dashboard">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" class="icon-sm" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12 11.54 2.71a.65.65 0 0 1 .92 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75"/>
        </svg>
        My broadcast
    </a>
</nav>
