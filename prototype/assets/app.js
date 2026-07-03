/* 厂智访客 · 原型交互脚本（仅用于演示效果） */
(function () {
  // 选择块（chip）单选
  document.addEventListener('click', function (e) {
    var chip = e.target.closest('.chip');
    if (chip && chip.parentElement.classList.contains('chip-group')) {
      chip.parentElement.querySelectorAll('.chip').forEach(function (c) { c.classList.remove('active'); });
      chip.classList.add('active');
    }

    // 标签切换（同容器 .tabs 内）
    var tab = e.target.closest('.tab');
    if (tab && tab.parentElement.classList.contains('tabs')) {
      tab.parentElement.querySelectorAll('.tab').forEach(function (t) { t.classList.remove('active'); });
      tab.classList.add('active');
    }

    // 触发提示
    var toastBtn = e.target.closest('[data-toast]');
    if (toastBtn) {
      showToast(toastBtn.getAttribute('data-toast'));
    }
  });

  // 简单的 Toast 提示
  window.showToast = function (msg) {
    var t = document.createElement('div');
    t.textContent = msg || '操作成功';
    t.style.cssText = 'position:fixed;left:50%;bottom:40px;transform:translateX(-50%) translateY(20px);' +
      'background:#101a33;color:#fff;padding:13px 22px;border-radius:12px;font-size:14px;font-weight:600;' +
      'box-shadow:0 16px 40px rgba(0,0,0,.3);z-index:9999;opacity:0;transition:all .25s ease;';
    document.body.appendChild(t);
    requestAnimationFrame(function () { t.style.opacity = '1'; t.style.transform = 'translateX(-50%) translateY(0)'; });
    setTimeout(function () {
      t.style.opacity = '0'; t.style.transform = 'translateX(-50%) translateY(20px)';
      setTimeout(function () { t.remove(); }, 300);
    }, 2000);
  };

  // 实时时钟（用于门卫工作台/看板）
  function tick() {
    var els = document.querySelectorAll('[data-clock]');
    if (!els.length) return;
    var now = new Date();
    var pad = function (n) { return (n < 10 ? '0' : '') + n; };
    var t = pad(now.getHours()) + ':' + pad(now.getMinutes()) + ':' + pad(now.getSeconds());
    var wk = ['星期日','星期一','星期二','星期三','星期四','星期五','星期六'][now.getDay()];
    var d = now.getFullYear() + '年' + (now.getMonth() + 1) + '月' + now.getDate() + '日 ' + wk;
    els.forEach(function (el) {
      var type = el.getAttribute('data-clock');
      el.textContent = type === 'date' ? d : t;
    });
  }
  tick();
  setInterval(tick, 1000);
})();
