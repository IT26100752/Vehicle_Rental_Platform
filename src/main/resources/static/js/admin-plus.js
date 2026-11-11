/* Extra admin effects: KPI count-up, cursor spotlight, staggered table rows, live table filter. */
(function () {
  'use strict';
  var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var shell = document.querySelector('.admin-main');
  if (!shell) return;

  // cursor spotlight
  shell.addEventListener('pointermove', function (e) {
    var el = e.target.closest ? e.target.closest('.kpi, .card') : null;
    if (!el) return;
    var r = el.getBoundingClientRect();
    el.style.setProperty('--mx', (e.clientX - r.left) + 'px');
    el.style.setProperty('--my', (e.clientY - r.top) + 'px');
  });

  // KPI numbers count up (plain integers only)
  if (!reduce) {
    shell.querySelectorAll('.kpi strong').forEach(function (el) {
      var txt = el.textContent.trim();
      if (!/^\d{1,7}$/.test(txt) || el.children.length) return;
      var target = parseInt(txt, 10);
      if (target < 2) return;
      var start = null;
      el.textContent = '0';
      function step(ts) {
        if (start === null) start = ts;
        var p = Math.min(1, (ts - start) / 900);
        el.textContent = String(Math.round(target * (1 - Math.pow(1 - p, 3))));
        if (p < 1) requestAnimationFrame(step); else el.textContent = String(target);
      }
      requestAnimationFrame(step);
    });
  }

  // staggered table rows
  if (!reduce) {
    shell.querySelectorAll('tbody').forEach(function (tb) {
      Array.prototype.slice.call(tb.rows, 0, 25).forEach(function (tr, i) {
        tr.style.setProperty('--i', i);
        tr.classList.add('row-in');
      });
    });
  }

  // live filter above every table with more than 6 rows
  shell.querySelectorAll('.table-wrap').forEach(function (wrap) {
    var table = wrap.querySelector('table');
    if (!table || !table.tBodies.length || table.tBodies[0].rows.length < 7) return;
    if (wrap.parentNode.previousElementSibling && wrap.parentNode.previousElementSibling.matches('form')) return;
    var input = document.createElement('input');
    input.type = 'search'; input.className = 'table-filter'; input.placeholder = 'Filter rows on this page';
    input.setAttribute('aria-label', 'Filter rows on this page');
    wrap.parentNode.insertBefore(input, wrap);
    input.addEventListener('input', function () {
      var q = input.value.trim().toLowerCase();
      Array.prototype.forEach.call(table.tBodies[0].rows, function (tr) {
        tr.hidden = q !== '' && tr.textContent.toLowerCase().indexOf(q) === -1;
      });
    });
  });
})();
