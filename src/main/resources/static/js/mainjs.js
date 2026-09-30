$(function () {
    var TOKEN_KEY = 'token';

    // ---------- Tiện ích ----------
    // Giải mã một phần của JWT (0 = header, 1 = payload). Chỉ để hiển thị;
    // việc kiểm tra chữ ký do server đảm nhiệm.
    function decodePart(token, index) {
        try {
            var part = token.split('.')[index].replace(/-/g, '+').replace(/_/g, '/');
            return JSON.parse(decodeURIComponent(escape(atob(part))));
        } catch (e) {
            return null;
        }
    }

    function formatTime(seconds) {
        return seconds ? new Date(seconds * 1000).toLocaleString('vi-VN') : '-';
    }

    function formatRemaining(ms) {
        var total = Math.max(0, Math.floor(ms / 1000));
        var h = Math.floor(total / 3600);
        var m = Math.floor((total % 3600) / 60);
        var s = total % 60;
        return (h ? h + ' giờ ' : '') + (h || m ? m + ' phút ' : '') + s + ' giây';
    }

    function renderToken(token) {
        var parts = token.split('.');
        var header = decodePart(token, 0);
        var claims = decodePart(token, 1);

        $('#rawToken').empty()
            .append($('<span class="h">').text(parts[0]))
            .append('.')
            .append($('<span class="p">').text(parts[1]))
            .append('.')
            .append($('<span class="s">').text(parts[2] || ''));
        $('#partHeader').text(header ? JSON.stringify(header, null, 2) : '-');
        $('#partPayload').text(claims ? JSON.stringify(claims, null, 2) : '-');

        if (!claims || !claims.exp) {
            return;
        }
        $('#tokenIat').text(formatTime(claims.iat));
        $('#tokenExp').text(formatTime(claims.exp));

        var expMs = claims.exp * 1000;
        var totalMs = claims.iat ? expMs - claims.iat * 1000 : null;

        function tick() {
            var left = expMs - Date.now();
            if (left <= 0) {
                goToLogin('expired');
                return;
            }
            var percent = totalMs ? Math.min(100, Math.round(left / totalMs * 100)) : 100;
            var low = percent <= 10;
            $('#remaining').text('Còn ' + formatRemaining(left)).toggleClass('low', low);
            $('#barFill').css('width', percent + '%').toggleClass('low', low);
            $('.bar').attr('aria-valuenow', percent);
        }
        tick();
        setInterval(tick, 1000);
    }

    // reason: 'expired' (token hết hạn/không hợp lệ), 'logout' (người dùng đăng xuất) hoặc bỏ trống
    function goToLogin(reason) {
        localStorage.removeItem(TOKEN_KEY);
        var query = reason === 'expired' ? '?expired=1' : (reason === 'logout' ? '?logout' : '');
        window.location.href = '/login' + query;
    }

    // ---------- Trang đăng nhập ----------
    var $loginForm = $('#loginForm');
    if ($loginForm.length) {
        var $feedback = $('#feedback');

        function showError(text) {
            $('#feedbackMessage').text(text);
            $feedback.removeClass('d-none');
        }

        if (new URLSearchParams(window.location.search).has('expired')) {
            showError('Phiên đăng nhập đã hết hạn hoặc token không hợp lệ. Vui lòng đăng nhập lại.');
        }

        $loginForm.on('submit', function (e) {
            e.preventDefault();
            $feedback.addClass('d-none');

            var $btn = $('#btn-submit').prop('disabled', true);
            var $label = $btn.find('span').text('Đang đăng nhập...');
            var body = JSON.stringify({
                email: $('#email').val().trim(),
                password: $('#password').val()
            });

            $.ajax({
                type: 'POST',
                url: '/auth/login',
                dataType: 'json',
                contentType: 'application/json; charset=utf-8',
                data: body,
                success: function (data) {
                    localStorage.setItem(TOKEN_KEY, data.token);
                    window.location.href = '/user/profile';
                },
                error: function (xhr) {
                    showError(xhr.status === 401
                        ? 'Email hoặc mật khẩu không đúng.'
                        : 'Không thể đăng nhập lúc này. Hãy kiểm tra máy chủ đang chạy rồi thử lại.');
                    $btn.prop('disabled', false);
                    $label.text('Đăng nhập');
                }
            });
        });
    }

    // ---------- Trang hồ sơ ----------
    if ($('#profile').length) {
        var token = localStorage.getItem(TOKEN_KEY);
        if (!token) {
            goToLogin();
            return;
        }

        renderToken(token);

        $.ajax({
            type: 'GET',
            url: '/users/me',
            dataType: 'json',
            beforeSend: function (xhr) {
                xhr.setRequestHeader('Authorization', 'Bearer ' + token);
            },
            success: function (data) {
                $('#profile').text(data.fullName);
                $('#userEmail').text(data.email);
                if (data.images) {
                    // Tài khoản cũ có thể lưu đường dẫn tương đối, nên chuẩn hóa về dạng /images/...
                    var src = /^(\/|https?:)/.test(data.images) ? data.images : '/' + data.images;
                    $('#images').attr('src', src);
                }
            },
            error: function () {
                // Token thiếu, sai chữ ký hoặc hết hạn: xóa token và quay lại trang đăng nhập
                goToLogin('expired');
            }
        });

        $('#logout').on('click', function () {
            goToLogin('logout');
        });
    }
});
