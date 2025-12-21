/**
 * User Interactions - Delayed Loading
 * 
 * This script loads user interaction elements (likes, comments, shares) 
 * after the main content has been rendered, ensuring AI crawlers only 
 * see the pure structured content.
 * 
 * Verification: Requirements 7.1, 7.2, 7.3, 7.4, 7.5
 */

(function() {
    'use strict';
    
    // Wait for DOM to be fully loaded
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initInteractions);
    } else {
        initInteractions();
    }
    
    function initInteractions() {
        const container = document.getElementById('user-interactions');
        
        if (!container) {
            console.warn('User interactions container not found');
            return;
        }
        
        const contentId = container.getAttribute('data-content-id');
        
        if (!contentId) {
            console.warn('Content ID not found');
            return;
        }
        
        // Get JWT token
        const token = localStorage.getItem('jwt_token') || sessionStorage.getItem('jwt_token');
        
        if (!token) {
            showLoginPrompt(container);
            return;
        }
        
        // Load interaction status and render UI
        loadInteractionStatus(contentId, token);
    }
    
    function showLoginPrompt(container) {
        container.innerHTML = `
            <div class="interaction-login-prompt">
                <p>请登录后进行互动操作</p>
                <a href="/login" class="btn-login">登录</a>
            </div>
        `;
    }
    
    async function loadInteractionStatus(contentId, token) {
        try {
            const response = await fetch(`/api/user/interact/status?contentId=${contentId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to load interaction status');
            }
            
            const result = await response.json();
            if (result.success) {
                renderInteractionUI(contentId, result.data, token);
                loadComments(contentId, token);
            }
        } catch (error) {
            console.error('Error loading interaction status:', error);
        }
    }
    
    function renderInteractionUI(contentId, status, token) {
        const container = document.getElementById('user-interactions');
        
        container.innerHTML = `
            <div class="interaction-panel">
                <div class="interaction-buttons">
                    <button class="btn-interaction ${status.hasLiked ? 'active' : ''}" 
                            data-action="like" 
                            data-content-id="${contentId}">
                        <span class="icon">👍</span>
                        <span class="label">点赞</span>
                        <span class="count">${status.likeCount}</span>
                    </button>
                    
                    <button class="btn-interaction ${status.hasFavorited ? 'active' : ''}" 
                            data-action="favorite" 
                            data-content-id="${contentId}">
                        <span class="icon">⭐</span>
                        <span class="label">收藏</span>
                        <span class="count">${status.favoriteCount}</span>
                    </button>
                    
                    <button class="btn-interaction" 
                            data-action="share" 
                            data-content-id="${contentId}">
                        <span class="icon">🔗</span>
                        <span class="label">分享</span>
                    </button>
                    
                    <button class="btn-interaction" 
                            data-action="comment" 
                            data-content-id="${contentId}">
                        <span class="icon">💬</span>
                        <span class="label">评论</span>
                        <span class="count">${status.commentCount}</span>
                    </button>
                </div>
                
                <div id="share-result" class="share-result" style="display: none;"></div>
                
                <div id="comment-section" class="comment-section" style="display: none;">
                    <div class="comment-form">
                        <textarea id="comment-input" 
                                  placeholder="发表你的评论..." 
                                  rows="3"></textarea>
                        <button id="submit-comment" class="btn-submit-comment">发表评论</button>
                    </div>
                    <div id="comments-list" class="comments-list"></div>
                </div>
            </div>
        `;
        
        // Attach event listeners
        attachInteractionListeners(token);
    }
    
    function attachInteractionListeners(token) {
        // Like and Favorite buttons (Requirement 7.1, 7.2)
        document.querySelectorAll('.btn-interaction[data-action="like"], .btn-interaction[data-action="favorite"]').forEach(button => {
            button.addEventListener('click', async function() {
                const action = this.dataset.action;
                const contentId = this.dataset.contentId;
                const isActive = this.classList.contains('active');
                
                try {
                    const method = isActive ? 'DELETE' : 'POST';
                    const response = await fetch(`/api/user/interact/${action}?contentId=${contentId}`, {
                        method: method,
                        headers: {
                            'Authorization': `Bearer ${token}`,
                            'Content-Type': 'application/json'
                        }
                    });
                    
                    if (!response.ok) {
                        throw new Error('Interaction failed');
                    }
                    
                    const result = await response.json();
                    if (result.success) {
                        // Toggle active state
                        this.classList.toggle('active');
                        
                        // Update count (AJAX update without page refresh - Requirement 7.1)
                        const countSpan = this.querySelector('.count');
                        let currentCount = parseInt(countSpan.textContent);
                        countSpan.textContent = isActive ? currentCount - 1 : currentCount + 1;
                    }
                } catch (error) {
                    console.error('Error performing interaction:', error);
                    alert('操作失败，请重试');
                }
            });
        });
        
        // Share button (Requirement 7.4 - generate link with nofollow)
        document.querySelector('.btn-interaction[data-action="share"]').addEventListener('click', async function() {
            const contentId = this.dataset.contentId;
            
            try {
                const response = await fetch(`/api/user/share?contentId=${contentId}`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });
                
                if (!response.ok) {
                    throw new Error('Share link generation failed');
                }
                
                const result = await response.json();
                if (result.success) {
                    const shareResult = document.getElementById('share-result');
                    shareResult.innerHTML = `
                        <div class="share-link-container">
                            <p>分享链接已生成：</p>
                            <input type="text" 
                                   value="${result.data.shareUrl}" 
                                   readonly 
                                   id="share-url-input">
                            <button onclick="window.copyShareLink()" class="btn-copy">复制链接</button>
                            <p class="share-html-note">HTML代码（带nofollow）：</p>
                            <code>${escapeHtml(result.data.htmlLink)}</code>
                        </div>
                    `;
                    shareResult.style.display = 'block';
                }
            } catch (error) {
                console.error('Error generating share link:', error);
                alert('生成分享链接失败，请重试');
            }
        });
        
        // Comment button
        document.querySelector('.btn-interaction[data-action="comment"]').addEventListener('click', function() {
            const commentSection = document.getElementById('comment-section');
            commentSection.style.display = commentSection.style.display === 'none' ? 'block' : 'none';
        });
        
        // Submit comment button (Requirement 7.5 - independent API loading)
        document.getElementById('submit-comment').addEventListener('click', async function() {
            const contentId = document.querySelector('.btn-interaction[data-action="comment"]').dataset.contentId;
            const commentText = document.getElementById('comment-input').value.trim();
            
            if (!commentText) {
                alert('请输入评论内容');
                return;
            }
            
            try {
                const response = await fetch('/api/user/comments', {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        contentId: parseInt(contentId),
                        commentText: commentText
                    })
                });
                
                if (!response.ok) {
                    throw new Error('Comment submission failed');
                }
                
                const result = await response.json();
                if (result.success) {
                    // Clear input
                    document.getElementById('comment-input').value = '';
                    
                    // Reload comments
                    loadComments(contentId, token);
                    
                    // Update comment count
                    const commentButton = document.querySelector('.btn-interaction[data-action="comment"]');
                    const countSpan = commentButton.querySelector('.count');
                    countSpan.textContent = parseInt(countSpan.textContent) + 1;
                    
                    alert('评论发表成功');
                }
            } catch (error) {
                console.error('Error submitting comment:', error);
                alert('评论发表失败，请重试');
            }
        });
    }
    
    // Load comments via independent API (Requirement 7.5)
    async function loadComments(contentId, token) {
        try {
            const response = await fetch(`/api/user/comments?contentId=${contentId}&page=0&size=10`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to load comments');
            }
            
            const result = await response.json();
            if (result.success) {
                renderComments(result.data);
            }
        } catch (error) {
            console.error('Error loading comments:', error);
        }
    }
    
    function renderComments(comments) {
        const commentsList = document.getElementById('comments-list');
        
        if (comments.length === 0) {
            commentsList.innerHTML = '<p class="no-comments">暂无评论</p>';
            return;
        }
        
        commentsList.innerHTML = comments.map(comment => `
            <div class="comment-item">
                <div class="comment-header">
                    <span class="comment-user">${comment.userEmail || '用户'}</span>
                    <span class="comment-time">${formatDate(comment.createdAt)}</span>
                </div>
                <div class="comment-text">${escapeHtml(comment.commentText)}</div>
            </div>
        `).join('');
    }
    
    // Global function for copy button
    window.copyShareLink = function() {
        const input = document.getElementById('share-url-input');
        input.select();
        document.execCommand('copy');
        alert('链接已复制到剪贴板');
    };
    
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    function formatDate(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diff = now - date;
        
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);
        
        if (minutes < 1) return '刚刚';
        if (minutes < 60) return `${minutes}分钟前`;
        if (hours < 24) return `${hours}小时前`;
        if (days < 7) return `${days}天前`;
        
        return date.toLocaleDateString('zh-CN');
    }
})();
