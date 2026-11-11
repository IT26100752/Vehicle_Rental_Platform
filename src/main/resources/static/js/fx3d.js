/* fx3d.js - pointer-driven 3D tilt, parallax and count-up. No dependencies. */
(function () {
  'use strict';
  var calm = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var fine = window.matchMedia && window.matchMedia('(hover: hover) and (pointer: fine)').matches;

  /* ---- login / register: tilting card + layered parallax + 3D rings ---- */
  var card = document.querySelector('.auth2-card');
  if (card && !calm) {
    var show = card.querySelector('.auth2-show');
    if (show) {
      var car = show.querySelector('.auth2-car');
      var orbit = document.createElement('div'); orbit.className = 'fx-orbit'; orbit.setAttribute('aria-hidden', 'true');
      orbit.innerHTML = '<i></i><i></i><i></i>';
      var road = document.createElement('div'); road.className = 'fx-road'; road.setAttribute('aria-hidden', 'true');
      if (car) { show.insertBefore(orbit, car); car.insertAdjacentElement('afterend', road); }
    }
    if (fine) {
      var host = card.parentElement, raf = 0;
      host.addEventListener('pointermove', function (e) {
        if (raf) return;
        raf = requestAnimationFrame(function () {
          raf = 0;
          var r = card.getBoundingClientRect();
          var x = (e.clientX - r.left) / r.width, y = (e.clientY - r.top) / r.height;
          var nx = Math.max(-1, Math.min(1, (x - .5) * 2)), ny = Math.max(-1, Math.min(1, (y - .5) * 2));
          card.style.setProperty('--ry', (nx * 5) + 'deg');
          card.style.setProperty('--rx', (-ny * 4) + 'deg');
          card.style.setProperty('--px', nx.toFixed(3)); card.style.setProperty('--py', ny.toFixed(3));
          card.style.setProperty('--mx', (x * 100) + '%'); card.style.setProperty('--my', (y * 100) + '%');
          card.style.setProperty('--glare', 1);
        });
      });
      host.addEventListener('pointerleave', function () {
        card.style.setProperty('--px', 0); card.style.setProperty('--py', 0);
        card.style.setProperty('--rx', '0deg'); card.style.setProperty('--ry', '0deg'); card.style.setProperty('--glare', 0);
      });
    }
  }

  /* ---- dashboards: tilt + glare on KPI tiles and vehicle cards ---- */
  if (!calm && fine) {
    document.querySelectorAll('.kpi, .vehicle-card').forEach(function (el) {
      el.setAttribute('data-fx-tilt', '');
      el.addEventListener('pointermove', function (e) {
        if (el.classList.contains('reveal') && !el.classList.contains('in')) return;
        var r = el.getBoundingClientRect();
        var x = (e.clientX - r.left) / r.width, y = (e.clientY - r.top) / r.height;
        el.style.transitionDelay = '0ms';
        el.style.transform = 'perspective(800px) rotateX(' + ((.5 - y) * 7).toFixed(2) + 'deg) rotateY(' + ((x - .5) * 9).toFixed(2) + 'deg) translateZ(0) scale(1.015)';
        el.style.setProperty('--mx', (x * 100) + '%'); el.style.setProperty('--my', (y * 100) + '%'); el.style.setProperty('--glare', 1);
      });
      el.addEventListener('pointerleave', function () {
        el.style.transform = ''; el.style.setProperty('--glare', 0);
        setTimeout(function () { el.style.transitionDelay = ''; }, 260);
      });
    });
  }

  /* ---- stagger KPI tiles + count-up for plain whole numbers ---- */
  document.querySelectorAll('.kpis, .grid').forEach(function (g) {
    Array.prototype.forEach.call(g.children, function (c, i) { if (c.classList.contains('kpi')) c.style.setProperty('--i', Math.min(i, 8)); });
  });
  if (calm) return;
  var nums = [];
  document.querySelectorAll('.kpi strong').forEach(function (s) {
    var t = s.textContent.trim();
    if (/^\d{1,7}$/.test(t) && s.children.length === 0 && !s.hasAttribute('data-count')) nums.push({ el: s, to: parseInt(t, 10) });
  });
  nums.forEach(function (n) {
    if (n.to === 0) return;
    n.el.textContent = '0';
    var start = null;
    function tick(ts) {
      if (start === null) start = ts;
      var p = Math.min(1, (ts - start) / 900), e = 1 - Math.pow(1 - p, 3);
      n.el.textContent = String(Math.round(n.to * e));
      if (p < 1) requestAnimationFrame(tick);
    }
    setTimeout(function () { requestAnimationFrame(tick); }, 150);
  });
})();
