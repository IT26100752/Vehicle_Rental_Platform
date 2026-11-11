/* Vehicle Rental Management System - front-end behaviour (no libraries).
 *  1. Loading screen while a page or form is really loading (no artificial wait)
 *  2. Confirm dialogs for destructive actions        4. Live availability + price check
 *  3. Toasts, mobile nav, tabs, password helpers     5. Card field formatting
 */
(function () {
  'use strict';

  document.documentElement.classList.add('js');
  var reduceMotion = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var OVERLAY_GRACE_MS = 0;    // car screen delay in ms: 0 = show at once on login / register (raise it to hide it for quick replies)
  var SLOW_MS = 8000;          // after this the overlay admits it is taking longer than usual
  var loaderTimer = null, slowTimer = null, topbar = null;
  var loader = document.getElementById('loader');
  var loaderText = document.getElementById('loaderText');
  var loaderSub = document.getElementById('loaderSub');
  var navigating = false;
  /* The car screen is only used for the sign-in and register pages (links to them and their forms).
     Everywhere else you get the thin gold bar on top. Add paths to this pattern to give another page the car. */
  var CAR_PAGES = /^\/(login|register|admin\/login)\/?$/;
  function carAllowed(url) {
    try { return CAR_PAGES.test(new URL(url, location.href).pathname); } catch (e) { return false; }
  }

  /* ---------------------------------------------------------- loader */
  function guessText(url) {
    var path = '';
    try { path = new URL(url, location.href).pathname; } catch (e) { /* ignore */ }
    if (path.indexOf('/logout') === 0) return ['Signing you out', 'See you on the road soon'];
    if (path.indexOf('/login') === 0) return ['Taking you to sign in', 'One moment'];
    if (path.indexOf('/register') === 0) return ['Taking you to sign up', 'One moment'];
    if (path.indexOf('/admin') === 0) return ['Opening the admin panel', 'Loading your data'];
    if (path.indexOf('/payments') === 0) return ['Opening payments', 'Your card details are never stored'];
    if (path.indexOf('/vehicles') === 0) return ['Finding vehicles', 'Checking what is free'];
    return ['Working on it', 'Just a moment'];
  }

  /* thin progress bar along the top of the window: instant feedback for every navigation */
  function barStart() {
    if (!topbar) {
      topbar = document.createElement('div');
      topbar.className = 'topbar';
      topbar.setAttribute('aria-hidden', 'true');
      topbar.innerHTML = '<i></i>';
      document.body.appendChild(topbar);
    }
    topbar.classList.remove('run');
    void topbar.offsetWidth;                       // restart the transition
    topbar.classList.add('run');
  }
  function barStop() { if (topbar) topbar.classList.remove('run'); }

  /* full-screen car overlay: only for form posts that are still pending (sign in, booking, e-mails ...) */
  function showLoader(text, sub) {
    if (!loader) return;
    loader.classList.add('show');
    loader.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
    window.setTimeout(function () {                // set the words after it is visible so screen readers announce them
      if (loaderText) loaderText.textContent = text || 'Working on it';
      if (loaderSub) loaderSub.textContent = sub || '';
    }, 40);
    window.clearTimeout(slowTimer);
    slowTimer = window.setTimeout(function () {
      if (loaderSub) loaderSub.textContent = 'Still working. This is taking a little longer than usual.';
    }, SLOW_MS);
  }

  function hideLoader() {
    navigating = false;
    window.clearTimeout(loaderTimer);
    window.clearTimeout(slowTimer);
    barStop();
    if (!loader) return;
    loader.classList.remove('show');
    loader.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
  }

  /** Navigate straight away. Links get the top bar; form posts also get the car overlay if they are slow. */
  function startLoader(text, sub, overlay) {
    if (navigating) return;
    navigating = true;
    barStart();
    if (overlay) {
      if (OVERLAY_GRACE_MS <= 0) showLoader(text, sub);
      else loaderTimer = window.setTimeout(function () { showLoader(text, sub); }, OVERLAY_GRACE_MS);
    }
    window.setTimeout(hideLoader, 30000);          // safety net (e.g. a download that never leaves the page)
  }

  window.addEventListener('pageshow', function (e) { if (e.persisted) hideLoader(); });

  document.addEventListener('click', function (e) {
    if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;
    var a = e.target.closest ? e.target.closest('a[href]') : null;
    if (!a) return;
    var href = a.getAttribute('href');
    if (!href || href.charAt(0) === '#' || href.indexOf('javascript:') === 0 || href.indexOf('mailto:') === 0 || href.indexOf('tel:') === 0) return;
    if (a.target && a.target !== '_self') return;
    if (a.hasAttribute('download') || a.hasAttribute('data-no-loader')) return;
    var url;
    try { url = new URL(a.href, location.href); } catch (err) { return; }
    if (url.origin !== location.origin) return;
    if (url.pathname === location.pathname && url.search === location.search && url.hash) return;
    var guess = guessText(a.href);
    startLoader(a.getAttribute('data-loader-text') || guess[0], a.getAttribute('data-loader-sub') || guess[1], a.getAttribute('data-loader') !== 'bar' && carAllowed(a.href));
  });

  document.addEventListener('submit', function (e) {
    var form = e.target;
    if (!form || form.tagName !== 'FORM' || e.defaultPrevented) return;
    if (form.hasAttribute('data-no-loader') || form.hasAttribute('data-pay-form')) return;
    var isGet = (form.getAttribute('method') || 'get').toLowerCase() === 'get';
    if (isGet && !form.hasAttribute('data-loader')) return;      // search / filter forms stay instant

    if (form.hasAttribute('data-confirm') && form.dataset.confirmed !== '1') {
      e.preventDefault();
      openConfirm(form);
      return;
    }
    if (navigating) { e.preventDefault(); return; }               // already sending: ignore double clicks
    if (!form.checkValidity()) return;                            // invalid: the server shows the field errors
    var action = form.getAttribute('action') || location.pathname;
    var guess = guessText(action);
    startLoader(form.getAttribute('data-loader-text') || guess[0], form.getAttribute('data-loader-sub') || guess[1],
      form.getAttribute('data-loader') !== 'bar' && carAllowed(action));
  });

  /* --------------------------------------------------------- confirm */
  function openConfirm(form) {
    var back = document.createElement('div');
    back.className = 'modal-back';
    back.innerHTML = '<div class="modal" role="alertdialog" aria-modal="true" aria-labelledby="mTitle" aria-describedby="mBody">' +
      '<h3 id="mTitle"></h3><p class="muted" id="mBody"></p>' +
      '<div class="row"><button type="button" class="btn ghost" data-act="no">Keep it</button>' +
      '<button type="button" class="btn danger" data-act="yes"></button></div></div>';
    back.querySelector('#mTitle').textContent = form.getAttribute('data-confirm-title') || 'Are you sure?';
    back.querySelector('#mBody').textContent = form.getAttribute('data-confirm');
    var yes = back.querySelector('[data-act="yes"]');
    yes.textContent = form.getAttribute('data-confirm-button') || 'Yes, continue';
    var no = back.querySelector('[data-act="no"]');
    var opener = document.activeElement;
    document.body.appendChild(back);
    no.focus();                                   // the safe choice is focused, not the destructive one
    function close(cancelled) {
      back.remove();
      document.removeEventListener('keydown', onKey);
      if (cancelled && form.hasAttribute('data-confirm-reset')) form.reset();   // e.g. put a dropdown back
      if (opener && opener.focus) opener.focus();
    }
    function onKey(ev) {
      if (ev.key === 'Escape') { close(true); return; }
      if (ev.key === 'Tab') {                     // keep Tab inside the dialog
        var first = no, last = yes;
        if (ev.shiftKey && document.activeElement === first) { ev.preventDefault(); last.focus(); }
        else if (!ev.shiftKey && document.activeElement === last) { ev.preventDefault(); first.focus(); }
      }
    }
    document.addEventListener('keydown', onKey);
    back.addEventListener('click', function (ev) {
      var act = ev.target.getAttribute && ev.target.getAttribute('data-act');
      if (ev.target === back || act === 'no') close(true);
      if (act === 'yes') { close(false); form.dataset.confirmed = '1'; form.requestSubmit(); }
    });
  }

  /* ---------------------------------------------------------- toasts */
  document.querySelectorAll('.toast').forEach(function (t) {
    function dismiss() { t.classList.add('leaving'); window.setTimeout(function () { t.remove(); }, 300); }
    var btn = t.querySelector('button');
    if (btn) btn.addEventListener('click', dismiss);
    var life = t.classList.contains('error') ? 14000 : 8000, timer = null;
    function arm() { window.clearTimeout(timer); timer = window.setTimeout(dismiss, life); }
    function hold() { window.clearTimeout(timer); }
    t.addEventListener('mouseenter', hold); t.addEventListener('focusin', hold);   // do not vanish while being read
    t.addEventListener('mouseleave', arm);  t.addEventListener('focusout', arm);
    arm();
  });


  /* ----------------------------------------------- dark / light mode */
  var themeBtn = document.getElementById('themeToggle');
  if (themeBtn) {
    var root = document.documentElement;
    function paintTheme() {
      var light = root.getAttribute('data-theme') === 'light';
      themeBtn.setAttribute('aria-pressed', light ? 'true' : 'false');
      themeBtn.title = light ? 'Switch to dark mode' : 'Switch to light mode';
    }
    paintTheme();
    themeBtn.addEventListener('click', function () {
      var next = root.getAttribute('data-theme') === 'light' ? 'dark' : 'light';
      root.classList.add('theme-anim');
      root.setAttribute('data-theme', next);
      try { localStorage.setItem('theme', next); } catch (e) { /* private mode */ }
      paintTheme();
      window.setTimeout(function () { root.classList.remove('theme-anim'); }, 450);
    });
  }


  /* --------------------------------------------- Sri Lanka map pins */
  var lk = document.getElementById('lkMap');
  if (lk) {
    var NS = 'http://www.w3.org/2000/svg';
    var lon0 = parseFloat(lk.getAttribute('data-lon0')), lat1 = parseFloat(lk.getAttribute('data-lat1'));
    var K = parseFloat(lk.getAttribute('data-k')), CS = parseFloat(lk.getAttribute('data-cos'));
    function px(lng) { return (lng - lon0) * K * CS; }
    function py(lat) { return (lat1 - lat) * K; }
    function el(name, attrs, parent) {
      var n = document.createElementNS(NS, name);
      for (var k in attrs) { if (Object.prototype.hasOwnProperty.call(attrs, k)) n.setAttribute(k, attrs[k]); }
      if (parent) parent.appendChild(n);
      return n;
    }
    var grid = lk.querySelector('.lk-grid'), routes = lk.querySelector('.lk-routes'), pinsG = lk.querySelector('.lk-pins');
    for (var la = 6; la <= 10; la++) el('line', { x1: 0, x2: 267.4, y1: py(la), y2: py(la) }, grid);
    for (var lo = 80; lo <= 82; lo++) el('line', { y1: 0, y2: 420, x1: px(lo), x2: px(lo) }, grid);

    var items = Array.prototype.slice.call(document.querySelectorAll('#pinList .pin-item'));
    var tip = document.getElementById('mapTip');
    var pts = items.map(function (li, i) {
      var x = px(parseFloat(li.getAttribute('data-lng'))), y = py(parseFloat(li.getAttribute('data-lat')));
      return { li: li, i: i, ox: x, oy: y, x: x, y: y, name: li.getAttribute('data-name') || '' };
    });
    // nudge pins that would sit on top of each other
    for (var it = 0; it < 60; it++) {
      for (var a = 0; a < pts.length; a++) for (var b = a + 1; b < pts.length; b++) {
        var dx = pts[b].x - pts[a].x, dy = pts[b].y - pts[a].y, d = Math.hypot(dx, dy), min = 15;
        if (d < min) {
          if (d < 0.01) { dx = 1; dy = 0; d = 1; }
          var push = (min - d) / 2, ux = dx / d, uy = dy / d;
          pts[a].x -= ux * push; pts[a].y -= uy * push; pts[b].x += ux * push; pts[b].y += uy * push;
        }
      }
    }
    var hub = pts.filter(function (p) { return /colombo/i.test(p.name); })[0] || pts[0];
    pts.forEach(function (p) {
      if (hub && p !== hub) {
        var mx = (hub.x + p.x) / 2, my = (hub.y + p.y) / 2, vx = p.x - hub.x, vy = p.y - hub.y, len = Math.hypot(vx, vy) || 1;
        el('path', { d: 'M' + hub.x.toFixed(1) + ' ' + hub.y.toFixed(1) + ' Q' + (mx - vy / len * 12).toFixed(1) + ' ' + (my + vx / len * 12).toFixed(1) + ' ' + p.x.toFixed(1) + ' ' + p.y.toFixed(1) }, routes);
      }
    });
    var groups = [];
    pts.forEach(function (p) {
      if (Math.hypot(p.x - p.ox, p.y - p.oy) > 2) {
        el('line', { x1: p.ox, y1: p.oy, x2: p.x, y2: p.y, 'class': 'lk-lead' }, pinsG);
        el('circle', { cx: p.ox, cy: p.oy, r: 1.6, 'class': 'lk-true' }, pinsG);
      }
    });
    pts.forEach(function (p) {
      var g = el('g', { transform: 'translate(' + p.x.toFixed(1) + ' ' + p.y.toFixed(1) + ')', 'class': 'lk-pin', tabindex: 0, role: 'button', 'aria-label': p.name }, pinsG);
      g.style.setProperty('--i', p.i);
      el('circle', { 'class': 'pulse', r: 4 }, g);
      var drop = el('g', { 'class': 'drop' }, g);
      drop.addEventListener('animationend', function () { g.classList.add('landed'); });
      var sc = el('g', { 'class': 'scale' }, drop);
      el('path', { 'class': 'body', d: 'M0 0 C-4 -6 -6.8 -9 -6.8 -13.2 A6.8 6.8 0 1 1 6.8 -13.2 C6.8 -9 4 -6 0 0Z' }, sc);
      var nm = el('text', { 'class': 'num', y: -10.6 }, sc); nm.textContent = String(p.i + 1);
      var lab = el('g', { 'class': 'lk-label' }, g);
      var tx = el('text', { y: -27, 'text-anchor': 'middle' }, lab); tx.textContent = p.name;
      var w = Math.max(40, p.name.length * 4.3 + 10);
      el('rect', { x: -w / 2, y: -35, width: w, height: 11, rx: 3 }, lab);
      lab.appendChild(tx);
      p.g = g; groups.push(g);
    });
    function show(p) {
      pts.forEach(function (q) { q.g.classList.toggle('on', q === p); q.li.classList.toggle('on', q === p); });
      if (p && tip) {
        var li = p.li, addr = li.querySelector('small');
        tip.textContent = p.name + (addr ? ' - ' + addr.textContent : '');
        p.g.parentNode.appendChild(p.g);
      }
    }
    pts.forEach(function (p) {
      ['mouseenter', 'focus', 'click'].forEach(function (ev) {
        p.g.addEventListener(ev, function () { show(p); });
        p.li.addEventListener(ev, function () { show(p); });
      });
      p.li.addEventListener('keydown', function (e) { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); show(p); } });
    });
    if (hub) window.setTimeout(function () { show(hub); }, 2200);
  }

  /* ------------------------------------------------------------- nav */
  var toggle = document.getElementById('navToggle');
  var links = document.getElementById('navLinks');
  if (toggle && links) {
    toggle.addEventListener('click', function () {
      var open = links.classList.toggle('open');
      toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
    });
  }

  /* ------------------------------------------------------------ tabs */
  document.querySelectorAll('[role="tablist"]').forEach(function (list) {
    var tabs = Array.prototype.slice.call(list.querySelectorAll('[role="tab"]'));
    function select(tab, focus) {
      tabs.forEach(function (t) {
        var on = t === tab;
        t.setAttribute('aria-selected', on ? 'true' : 'false');
        t.tabIndex = on ? 0 : -1;
        var panel = document.getElementById(t.getAttribute('aria-controls'));
        if (panel) panel.hidden = !on;
      });
      if (focus) tab.focus();
    }
    tabs.forEach(function (tab, i) {
      tab.addEventListener('click', function () { select(tab, false); });
      tab.addEventListener('keydown', function (e) {
        var n = null;
        if (e.key === 'ArrowRight' || e.key === 'ArrowDown') n = tabs[(i + 1) % tabs.length];
        else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') n = tabs[(i - 1 + tabs.length) % tabs.length];
        else if (e.key === 'Home') n = tabs[0];
        else if (e.key === 'End') n = tabs[tabs.length - 1];
        if (n) { e.preventDefault(); select(n, true); }
      });
    });
    var current = tabs.filter(function (t) { return t.getAttribute('aria-selected') === 'true'; })[0] || tabs[0];
    if (current) tabs.forEach(function (t) { t.tabIndex = t === current ? 0 : -1; });
  });

  /* ------------------------------------------------------- passwords */
  document.querySelectorAll('.pw-toggle').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var input = btn.parentElement.querySelector('input');
      var show = input.type === 'password';
      input.type = show ? 'text' : 'password';
      btn.textContent = show ? 'Hide' : 'Show';
    });
  });
  document.querySelectorAll('[data-strength]').forEach(function (input) {
    var bar = document.getElementById(input.getAttribute('data-strength'));
    if (!bar) return;
    var label = null;
    input.addEventListener('input', function () {
      var v = input.value, score = 0;
      if (v.length >= 8) score++;
      if (/[A-Za-z]/.test(v) && /\d/.test(v)) score++;
      if (v.length >= 12) score++;
      if (/[^A-Za-z0-9]/.test(v) && /[A-Z]/.test(v)) score++;
      bar.style.width = (score * 25) + '%';
      bar.style.background = score <= 1 ? 'var(--tail)' : score === 2 ? 'var(--sodium)' : 'var(--signal)';
      if (!label) {
        label = document.createElement('small');
        label.className = 'strength-text';
        label.setAttribute('aria-live', 'polite');
        var host = bar.closest('.strength') || bar.parentElement;
        host.parentNode.insertBefore(label, host.nextSibling);
      }
      label.textContent = !v ? '' : score <= 1 ? 'Weak password' : score === 2 ? 'Fair password' : score === 3 ? 'Good password' : 'Strong password';
    });
  });

  /* -------------------------------------------- card field formatting */
  document.querySelectorAll('[data-card-number]').forEach(function (input) {
    var preview = document.getElementById('cardPreviewNumber');
    input.addEventListener('input', function () {
      var digits = input.value.replace(/\D/g, '').slice(0, 19);
      input.value = digits.replace(/(.{4})/g, '$1 ').trim();
      if (preview) preview.textContent = input.value || '#### #### #### ####';
    });
  });
  document.querySelectorAll('[data-expiry]').forEach(function (input) {
    var preview = document.getElementById('cardPreviewExpiry');
    input.addEventListener('input', function () {
      var d = input.value.replace(/\D/g, '').slice(0, 4);
      if (d.length >= 3) d = d.slice(0, 2) + '/' + d.slice(2);
      input.value = d;
      if (preview) preview.textContent = d || 'MM/YY';
    });
  });
  document.querySelectorAll('[data-cvv]').forEach(function (input) {
    input.addEventListener('input', function () { input.value = input.value.replace(/\D/g, '').slice(0, 4); });
  });
  var holder = document.querySelector('[data-card-holder]');
  var holderPreview = document.getElementById('cardPreviewHolder');
  if (holder && holderPreview) {
    holder.addEventListener('input', function () { holderPreview.textContent = holder.value.toUpperCase() || 'CARD HOLDER'; });
  }

  /* --------------------- admin return form: live late-fine preview */
  document.querySelectorAll('[data-fine-preview]').forEach(function (root) {
    var input = root.querySelector('[data-actual-date]');
    var out = root.querySelector('.fine-out');
    var url = root.getAttribute('data-url');
    function update() {
      if (!input.value) { out.textContent = ''; return; }
      fetch(url + '?date=' + input.value, { headers: { 'Accept': 'application/json' } })
        .then(function (r) { return r.json(); })
        .then(function (d) {
          out.className = 'fine-out alert ' + (d.lateDays > 0 ? 'warn' : 'success');
          out.textContent = d.lateDays > 0
            ? d.lateDays + ' day(s) late. Fine LKR ' + money(d.fine) + ', final amount LKR ' + money(d.total) + '.'
            : 'On time. No fine.';
        })
        .catch(function () { out.textContent = ''; });
    }
    input.addEventListener('change', update);
    update();
  });

  /* ------------------------------ payment window countdown */
  document.querySelectorAll('[data-countdown]').forEach(function (el) {
    var until = new Date(el.getAttribute('data-countdown')).getTime();
    if (isNaN(until)) return;
    function tick() {
      var left = Math.max(0, Math.round((until - Date.now()) / 1000));
      var m = Math.floor(left / 60), s = ('0' + (left % 60)).slice(-2);
      el.textContent = left > 0 ? m + ':' + s : '0:00';
      if (left === 0) {
        el.classList.add('expired');
        document.querySelectorAll('[data-pay-button]').forEach(function (btn) {
          btn.classList.add('disabled'); btn.setAttribute('aria-disabled', 'true'); btn.tabIndex = -1;
          if ('disabled' in btn && btn.tagName === 'BUTTON') btn.disabled = true;
          btn.addEventListener('click', function (ev) { ev.preventDefault(); ev.stopPropagation(); }, true);
        });
        var note = el.closest('[data-hold-note]');
        if (note) {
          note.classList.remove('warn'); note.classList.add('error');
          note.innerHTML = '<div><strong>Your 15-minute hold has ended.</strong> The vehicle may no longer be held for you. ' +
            '<a href="/vehicles">Find a vehicle and book again</a>.</div>';
        } else { el.textContent = 'ended'; }
        window.clearInterval(timer);
      }
    }
    var timer = window.setInterval(tick, 1000);
    tick();
  });

  /* ------------------------------------------- picture preview in forms */
  document.querySelectorAll('select[data-preview]').forEach(function (sel) {
    var img = document.getElementById(sel.getAttribute('data-preview'));
    if (!img) return;
    sel.addEventListener('change', function () {
      var typeSel = document.getElementById('type');
      var photos = { CAR: 'car', VAN: 'van', BIKE: 'bike', TRUCK: 'truck' };
      var fallback = '/images/photos/' + (photos[typeSel && typeSel.value] || 'car') + '.jpg';
      img.src = sel.value || fallback;
    });
  });

  /* ------------------------------ dates + live availability and price */
  function money(n) {
    return Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
  function esc(t) {
    return String(t == null ? '' : t).replace(/[&<>"]/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]; });
  }
  function addDays(iso, n) {
    var d = new Date(iso + 'T00:00:00');
    d.setDate(d.getDate() + n);
    var m = ('0' + (d.getMonth() + 1)).slice(-2), day = ('0' + d.getDate()).slice(-2);
    return d.getFullYear() + '-' + m + '-' + day;
  }

  document.querySelectorAll('[data-availability]').forEach(function (root) {
    var vehicleId = root.getAttribute('data-vehicle-id');
    var pickup = root.querySelector('[data-pickup]');
    var ret = root.querySelector('[data-return]');
    var box = root.querySelector('.availability-box');
    var submits = Array.prototype.slice.call(document.querySelectorAll('[data-needs-available]')).filter(function (b) {
      return root.contains(b) || (b.getAttribute('form') && b.getAttribute('form') === root.id);
    });
    var quoteBody = document.querySelector('[data-quote-body]');   // when the page has a side card the price lines go there
    var timer = null, token = 0;
    function lockSubmits(locked) {
      submits.forEach(function (b) { b.disabled = locked; });
    }
    function setQuote(html) { if (quoteBody) quoteBody.innerHTML = html; }

    function setState(kind, html) {
      if (!box) return;
      box.className = 'availability-box' + (kind ? ' ' + kind : '');
      box.innerHTML = html;
    }
    function check() {
      if (pickup.value) ret.min = addDays(pickup.value, 1);
      if (pickup.value && ret.value && ret.value <= pickup.value) ret.value = '';
      if (!pickup.value || !ret.value) {
        setState('', '<span class="muted">Choose your pickup and return dates to see the price and availability.</span>');
        setQuote('<span class="muted small">Choose your dates to see the price.</span>');
        lockSubmits(true);
        return;
      }
      var mine = ++token;
      setState('', '<span class="muted">Checking availability...</span>');
      var extra = '';
      var drv = root.querySelector('[name="withDriver"]:checked');
      if (drv) extra += '&withDriver=' + encodeURIComponent(drv.value);
      ['pickupLocationId', 'dropLocationId'].forEach(function (n) {
        var el = root.querySelector('[name="' + n + '"]');
        if (el && el.value) extra += '&' + n + '=' + encodeURIComponent(el.value);
      });
      var promoEl = root.querySelector('[name="promoCode"]');
      if (promoEl && promoEl.value.trim()) extra += '&promo=' + encodeURIComponent(promoEl.value.trim());
      fetch('/api/vehicles/' + vehicleId + '/availability?pickup=' + pickup.value + '&ret=' + ret.value + extra,
        { headers: { 'Accept': 'application/json' } })
        .then(function (r) { return r.json(); })
        .then(function (a) {
          if (mine !== token) return;
          if (a.available) {
            var status = '<strong>Available.</strong> ' + a.days + ' day' + (a.days === 1 ? '' : 's');
            var lines = '';
            if (a.lines && a.lines.length) {
              lines = '<ul class="price-lines">';
              a.lines.forEach(function (l, i) {
                var neg = Number(l.amount) < 0;
                lines += '<li class="' + (neg ? 'neg' : '') + '" style="animation-delay:' + (i * 50) + 'ms"><span>' + esc(l.label) +
                  '</span><span>' + (neg ? '- ' : '') + 'LKR ' + money(Math.abs(l.amount)) + '</span></li>';
              });
              lines += '<li class="total"><span>Total</span><span>LKR ' + money(a.total) + '</span></li></ul>';
            }
            if (quoteBody) {                       // side card: the price lines; form: just the status
              setQuote(lines || '<div class="quote-total">LKR ' + money(a.total) + '</div>');
              setState('ok', status);
            } else {                               // no side card (vehicle page): everything in the box, as before
              setState('ok', status + (lines || ', total <strong>LKR ' + money(a.total) + '</strong>'));
            }
          } else {
            setState('bad', '<strong>Not available.</strong> ' + a.message);
            setQuote('<span class="muted small">Not available for these dates.</span>');
          }
          lockSubmits(!a.available);
        })
        .catch(function () {
          if (mine !== token) return;
          setState('bad', 'Could not check availability right now. Change a date or <button type="button" class="linklike" data-retry>try again</button>.');
          var rb = box && box.querySelector('[data-retry]');
          if (rb) rb.addEventListener('click', check);
          setQuote('<span class="muted small">Price unavailable right now.</span>');
        });
    }
    function debounced() { window.clearTimeout(timer); timer = window.setTimeout(check, 250); }
    pickup.addEventListener('change', debounced);
    ret.addEventListener('change', debounced);
    root.querySelectorAll('[name="withDriver"], [name="pickupLocationId"], [name="dropLocationId"], [name="promoCode"]').forEach(function (el) {
      el.addEventListener('change', debounced);
    });
    check();
  });

  /* =====================================================================
     UI ANIMATIONS + HELPERS (added)
     ===================================================================== */

  /* ------------------------------------------- scroll reveal (stagger) */
  if (!reduceMotion && 'IntersectionObserver' in window) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (en) {
        if (en.isIntersecting) { en.target.classList.add('in'); io.unobserve(en.target); }
      });
    }, { threshold: 0.08, rootMargin: '0px 0px -30px 0px' });
    document.querySelectorAll('main .card, main .review, main .vehicle-card, main .order, main .notif, main .type-tile, main .band, .admin-main .card, .admin-main .kpi, .admin-main .review').forEach(function (el) {
      if (el.closest('.modal, .loader, .pay-overlay')) return;
      var siblings = el.parentElement ? Array.prototype.slice.call(el.parentElement.children) : [el];
      el.style.setProperty('--i', Math.min(siblings.indexOf(el), 8));
      el.classList.add('reveal');
      io.observe(el);
    });
  }

  /* ------------------------------------------------------- count up */
  function countUp(el) {
    var target = parseFloat(el.getAttribute('data-count'));
    if (isNaN(target)) return;
    var dec = parseInt(el.getAttribute('data-decimals') || '0', 10);
    var prefix = el.getAttribute('data-prefix') || '', suffix = el.getAttribute('data-suffix') || '';
    function fmt(v) { return prefix + v.toLocaleString('en-US', { minimumFractionDigits: dec, maximumFractionDigits: dec }) + suffix; }
    if (reduceMotion) { el.textContent = fmt(target); return; }
    var start = null, dur = 1100;
    function step(ts) {
      if (start === null) start = ts;
      var p = Math.min(1, (ts - start) / dur), eased = 1 - Math.pow(1 - p, 3);
      el.textContent = fmt(target * eased);
      if (p < 1) window.requestAnimationFrame(step); else el.textContent = fmt(target);
    }
    window.requestAnimationFrame(step);
  }
  var counters = document.querySelectorAll('[data-count]');
  if (counters.length) {
    if ('IntersectionObserver' in window) {
      var cio = new IntersectionObserver(function (entries) {
        entries.forEach(function (en) { if (en.isIntersecting) { countUp(en.target); cio.unobserve(en.target); } });
      }, { threshold: 0.3 });
      counters.forEach(function (c) { cio.observe(c); });
    } else { counters.forEach(countUp); }
  }

  /* ------------------------------------------------- button ripple */
  document.addEventListener('pointerdown', function (e) {
    var b = e.target.closest ? e.target.closest('.btn') : null;
    if (!b || reduceMotion) return;
    var r = b.getBoundingClientRect(), size = Math.max(r.width, r.height);
    var dot = document.createElement('span');
    dot.className = 'ripple';
    dot.style.cssText = 'width:' + size + 'px;height:' + size + 'px;left:' + (e.clientX - r.left - size / 2) + 'px;top:' + (e.clientY - r.top - size / 2) + 'px';
    b.appendChild(dot);
    window.setTimeout(function () { dot.remove(); }, 650);
  });

  /* ---------------------- auto-submit filter forms (chips etc.) */
  document.querySelectorAll('form[data-autosubmit]').forEach(function (f) {
    function go() { if (f.requestSubmit) f.requestSubmit(); else f.submit(); }
    f.querySelectorAll('[data-autosubmit-trigger], select, input[type="radio"], input[type="number"]').forEach(function (el) {
      el.addEventListener('change', go);
    });
    var d1 = f.querySelector('input[name="pickupDate"]'), d2 = f.querySelector('input[name="returnDate"]');
    [d1, d2].forEach(function (d) {
      if (d) d.addEventListener('change', function () { if ((d1 && d1.value) === (d2 && d2.value) || (d1 && d1.value && d2 && d2.value)) go(); });
    });
  });
  /* ------- keep return date after pickup date on every search form */
  document.querySelectorAll('form').forEach(function (f) {
    if (f.hasAttribute('data-availability')) return;
    var d1 = f.querySelector('input[name="pickupDate"]'), d2 = f.querySelector('input[name="returnDate"]');
    if (!d1 || !d2) return;
    function link() {
      if (!d1.value) return;
      d2.min = addDays(d1.value, 1);
      if (d2.value && d2.value <= d1.value) d2.value = '';
    }
    d1.addEventListener('change', link);
    link();
  });

  /* --------------------------------- method picker + conditional blocks */
  function checkedValue(name, scope) {
    var els = (scope || document).querySelectorAll('[name="' + name + '"]');
    if (!els.length) return null;
    var first = els[0];
    if (first.type === 'radio') {
      for (var i = 0; i < els.length; i++) if (els[i].checked) return els[i].value;
      return null;
    }
    if (first.type === 'checkbox') return first.checked ? 'true' : 'false';
    return first.value;
  }
  function syncMethodPanels() {
    document.querySelectorAll('.method-picker').forEach(function (picker) {
      var scope = picker.closest('form') || document;
      var radio = picker.querySelector('input[type="radio"]');
      if (!radio) return;
      var value = checkedValue(radio.name, scope);
      scope.querySelectorAll('.method-panel[data-method-panel]').forEach(function (panel) {
        var on = panel.getAttribute('data-method-panel') === value;
        panel.classList.toggle('active', on);
        panel.querySelectorAll('input, select, textarea').forEach(function (c) { c.disabled = !on; });
      });
    });
  }
  function syncShowIf() {
    document.querySelectorAll('[data-show-if]').forEach(function (el) {
      var rule = el.getAttribute('data-show-if').split('=');
      var current = checkedValue(rule[0].trim(), el.closest('form') || document);
      var wanted = (rule[1] || '').split('|');
      var open = current !== null && wanted.indexOf(current) >= 0;
      el.classList.toggle('is-open', open);
      if (el.hasAttribute('data-show-disable')) {
        el.querySelectorAll('input, select, textarea').forEach(function (c) { c.disabled = !open; });
      }
    });
  }
  document.addEventListener('change', function () { syncMethodPanels(); syncShowIf(); });
  syncMethodPanels();
  syncShowIf();

  /* ------------------------------------------------ bell badge polling */
  var bell = document.querySelector('[data-bell]');
  if (bell) {
    var lastUnread = (function () { var b = bell.querySelector('.bell-badge'); return b ? parseInt(b.textContent, 10) || 0 : 0; })();
    var sideBadge = document.querySelector('.side-badge');
    window.setInterval(function () {
      if (document.hidden) return;
      fetch('/api/notifications/summary', { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' })
        .then(function (r) { return r.ok ? r.json() : null; })
        .then(function (d) {
          if (!d || typeof d.unread !== 'number') return;
          var badge = bell.querySelector('.bell-badge');
          if (d.unread > 0) {
            if (!badge) { badge = document.createElement('span'); badge.className = 'bell-badge'; bell.appendChild(badge); }
            badge.textContent = d.unread > 99 ? '99+' : d.unread;
            if (d.unread > lastUnread) {
              bell.classList.remove('has-new'); void bell.offsetWidth; bell.classList.add('has-new');
            }
          } else if (badge) { badge.remove(); }
          if (sideBadge) sideBadge.textContent = d.unread;
          lastUnread = d.unread;
        })
        .catch(function () { /* offline - try again next time */ });
    }, 30000);
  }

  /* =====================================================================
     PAYMENT VERIFICATION: form[data-pay-form][data-process-url]
     ===================================================================== */
  var overlay = document.getElementById('payOverlay');
  var payTitle = document.getElementById('payTitle');
  var paySub = document.getElementById('paySub');
  var payStepEls = overlay ? overlay.querySelectorAll('.pay-steps li') : [];

  function setPayState(state, title, sub) {
    if (!overlay) return;
    overlay.setAttribute('data-state', state);
    if (title && payTitle) payTitle.textContent = title;
    if (sub !== undefined && paySub) paySub.textContent = sub;
  }
  function openPay() {
    overlay.classList.remove('show'); void overlay.offsetWidth;
    overlay.querySelectorAll('.spark').forEach(function (n) { n.remove(); });
    payStepEls.forEach(function (li) { li.className = ''; });
    setPayState('processing', 'Verifying your payment', 'Please do not close this page');
    overlay.classList.add('show');
    overlay.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
  }
  function closePay() {
    overlay.classList.remove('show');
    overlay.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
  }
  function sparks() {
    var box = overlay.querySelector('.pay-visual');
    if (!box || reduceMotion) return;
    for (var i = 0; i < 14; i++) {
      var s = document.createElement('i'), a = (Math.PI * 2 * i) / 14, d = 70 + Math.random() * 30;
      s.className = 'spark';
      s.style.setProperty('--dx', Math.round(Math.cos(a) * d) + 'px');
      s.style.setProperty('--dy', Math.round(Math.sin(a) * d) + 'px');
      box.appendChild(s);
    }
  }
  function showPayErrors(form, data) {
    form.querySelectorAll('.pay-error').forEach(function (n) { n.remove(); });
    form.querySelectorAll('.field.invalid').forEach(function (f) { f.classList.remove('invalid'); var e = f.querySelector('.field-error.js-err'); if (e) e.remove(); });
    var box = document.createElement('div');
    box.className = 'alert error pay-error';
    box.setAttribute('role', 'alert');
    box.innerHTML = '<div>' + esc(data.message || 'The payment could not be completed. Please check your details and try again.') + '</div>';
    form.insertBefore(box, form.firstChild);
    if (data.errors) {
      Object.keys(data.errors).forEach(function (name) {
        var input = form.querySelector('[name="' + name + '"]');
        if (!input) return;
        var field = input.closest('.field');
        if (!field) return;
        field.classList.add('invalid');
        var err = document.createElement('div');
        err.className = 'field-error js-err';
        err.textContent = data.errors[name];
        field.appendChild(err);
      });
    }
    box.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth', block: 'center' });
  }

  document.addEventListener('submit', function (e) {
    var form = e.target;
    if (!form || form.tagName !== 'FORM' || !form.hasAttribute('data-pay-form') || !overlay || !window.fetch) return;
    var url = form.getAttribute('data-process-url');
    if (!url || form.dataset.paying === '1') { if (form.dataset.paying === '1') e.preventDefault(); return; }
    e.preventDefault();
    form.dataset.paying = '1';
    openPay();

    var minMs = reduceMotion ? 600 : 2400, started = Date.now(), stepIdx = 0;
    function advance() {
      if (stepIdx > 0 && payStepEls[stepIdx - 1]) payStepEls[stepIdx - 1].className = 'done';
      if (stepIdx < payStepEls.length) { payStepEls[stepIdx].className = 'active'; stepIdx++; }
    }
    advance();
    var stepTimer = window.setInterval(function () { if (stepIdx < payStepEls.length) advance(); }, minMs / Math.max(1, payStepEls.length));

    var request = fetch(url, {
      method: 'POST', body: new FormData(form), credentials: 'same-origin',
      headers: { 'Accept': 'application/json', 'X-Requested-With': 'XMLHttpRequest' }
    }).then(function (r) {
      var type = r.headers.get('content-type') || '';
      if (type.indexOf('json') < 0) throw new Error('not-json');
      return r.json();
    });

    request.then(function (data) {
      var wait = Math.max(0, minMs - (Date.now() - started));
      window.setTimeout(function () {
        window.clearInterval(stepTimer);
        payStepEls.forEach(function (li) { li.className = 'done'; });
        var status = data.status || (data.ok ? 'PAID' : 'FAILED');
        if (data.ok && status === 'PENDING_VERIFICATION') {
          setPayState('pending', 'Payment submitted', data.message || 'Our team will verify it shortly. You will be notified.');
        } else if (data.ok) {
          setPayState('success', 'Payment verified', data.message || 'Your booking is confirmed');
          sparks();
        } else {
          setPayState('failed', 'Payment could not be verified', data.message || 'Please check your details and try again');
        }
        if (data.ok && data.redirectUrl) {
          window.setTimeout(function () { window.location.href = data.redirectUrl; }, reduceMotion ? 800 : 2000);
        } else {
          window.setTimeout(function () {
            closePay();
            form.dataset.paying = '0';
            showPayErrors(form, data);
          }, reduceMotion ? 800 : 2200);
        }
      }, wait);
    }).catch(function () {
      // Could not talk to the JSON endpoint: fall back to a normal form post.
      window.clearInterval(stepTimer);
      closePay();
      form.dataset.paying = '0';
      form.removeAttribute('data-pay-form');
      form.submit();
    });
  });

  /* =====================================================================
     ACCESSIBILITY + LONG LISTS (added in the UI review pass)
     ===================================================================== */
  /* ---- skip link target + aria-current for the active item */
  (function () {
    var main = document.querySelector('main, .admin-main, .auth2, .auth-wrap');
    if (main) { if (!main.id) main.id = 'main-content'; if (!main.hasAttribute('tabindex')) main.tabIndex = -1; }
    var skip = document.querySelector('.skip-link');
    if (skip && main) skip.setAttribute('href', '#' + main.id);
    else if (skip) skip.remove();
    document.querySelectorAll('.nav-links a.active, .sidebar a.active, .tab-link.active').forEach(function (a) {
      a.setAttribute('aria-current', 'page');
    });
    document.querySelectorAll('.steps li.current').forEach(function (li) { li.setAttribute('aria-current', 'step'); });
  })();

  /* ---- Escape closes the mobile menu */
  document.addEventListener('keydown', function (e) {
    if (e.key !== 'Escape') return;
    var nl = document.getElementById('navLinks'), nt = document.getElementById('navToggle');
    if (nl && nl.classList.contains('open')) { nl.classList.remove('open'); if (nt) { nt.setAttribute('aria-expanded', 'false'); nt.focus(); } }
  });

  /* ---- client-side pagination: any table (or [data-paginate] list) with more than a page of rows */
  (function () {
    var DEFAULT_SIZE = 15;
    function attach(items, host, size, noun) {
      var pages = Math.ceil(items.length / size), page = 1;
      var nav = document.createElement('nav');
      nav.className = 'pager';
      nav.setAttribute('aria-label', 'Pages of ' + noun);
      nav.innerHTML = '<button type="button" class="btn small ghost" data-prev>Previous</button>' +
        '<span class="pager-info" aria-live="polite"></span>' +
        '<button type="button" class="btn small ghost" data-next>Next</button>';
      host.parentNode.insertBefore(nav, host.nextSibling);
      var prev = nav.querySelector('[data-prev]'), next = nav.querySelector('[data-next]'), info = nav.querySelector('.pager-info');
      function render() {
        items.forEach(function (it, i) { it.hidden = !(i >= (page - 1) * size && i < page * size); });
        var from = (page - 1) * size + 1, to = Math.min(items.length, page * size);
        info.textContent = 'Showing ' + from + '-' + to + ' of ' + items.length + ' (page ' + page + ' of ' + pages + ')';
        prev.disabled = page === 1; next.disabled = page === pages;
      }
      prev.addEventListener('click', function () { if (page > 1) { page--; render(); } });
      next.addEventListener('click', function () { if (page < pages) { page++; render(); } });
      render();
      return { refresh: function () { items = Array.prototype.slice.call(host.querySelector('tbody') ? host.querySelector('tbody').rows : items); page = 1; render(); } };
    }
    document.querySelectorAll('table').forEach(function (t) {
      if (t.hasAttribute('data-no-paginate') || !t.tBodies[0]) return;
      var rows = Array.prototype.slice.call(t.tBodies[0].rows);
      var size = parseInt(t.getAttribute('data-page-size') || DEFAULT_SIZE, 10);
      if (rows.length > size) t._pager = attach(rows, t.closest('.table-wrap') || t, size, 'rows');
    });
    document.querySelectorAll('[data-paginate]').forEach(function (list) {
      var items = Array.prototype.slice.call(list.children);
      var size = parseInt(list.getAttribute('data-paginate') || DEFAULT_SIZE, 10) || DEFAULT_SIZE;
      if (items.length > size) attach(items, list, size, 'items');
    });
  })();

  /* =====================================================================
     BATCH 2: validation, NIC auto-fill, calendar, phone tab bar, sortable
     tables + CSV, bell dropdown, admin counts, FAQ search
     ===================================================================== */
  function pad2(n) { return ('0' + n).slice(-2); }
  function isoDate(d) { return d.getFullYear() + '-' + pad2(d.getMonth() + 1) + '-' + pad2(d.getDate()); }
  function parseIso(v) { return /^\d{4}-\d{2}-\d{2}$/.test(v || '') ? new Date(v + 'T00:00:00') : null; }
  function startOfToday() { var t = new Date(); return new Date(t.getFullYear(), t.getMonth(), t.getDate()); }
  function fire(el, type) { el.dispatchEvent(new Event(type, { bubbles: true })); }

  /* ------------------------------------------------ NIC -> sex and age */
  function parseNic(raw) {
    var v = (raw || '').trim().toUpperCase(), year, days;
    if (/^[0-9]{12}$/.test(v)) { year = +v.slice(0, 4); days = +v.slice(4, 7); }
    else if (/^[0-9]{9}[VX]$/.test(v)) { year = 1900 + (+v.slice(0, 2)); days = +v.slice(2, 5); }
    else return null;
    var female = days > 500;
    if (female) days -= 500;
    if (days < 1 || days > 366) return null;
    var leap = (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0;
    if (!leap && days > 59) days -= 1;                 // NIC numbers every year as if it had 29 February
    var birth = new Date(year, 0, days);
    if (birth.getFullYear() !== year) return null;
    var now = new Date(), age = now.getFullYear() - year;
    if (now < new Date(now.getFullYear(), birth.getMonth(), birth.getDate())) age--;
    if (age < 0 || age > 120) return null;
    return { sex: female ? 'Female' : 'Male', age: age };
  }

  document.querySelectorAll('[data-nic]').forEach(function (nic) {
    var form = nic.form, sex = form.querySelector('[data-nic-sex]'), age = form.querySelector('[data-nic-age]');
    var summary = form.querySelector('[data-nic-summary]'), text = form.querySelector('[data-nic-text]');
    var edit = form.querySelector('[data-nic-edit]');
    if (!sex || !age) return;
    var fields = [sex.closest('.field'), age.closest('.field')];
    var editing = false;
    function showFields(on) { fields.forEach(function (f) { if (f) f.classList.toggle('derived', !on); }); if (summary) summary.hidden = on; }
    function run() {
      var info = parseNic(nic.value);
      if (!info) { showFields(true); return; }
      sex.value = info.sex; age.value = info.age;
      fire(sex, 'change'); fire(age, 'change');
      if (info.age >= 18 && info.age <= 80 && !editing) {
        if (text) text.textContent = info.sex + ', ' + info.age + ' years old';
        showFields(false);
      } else {
        showFields(true);                               // under 18 / over 80: show the age so the problem is visible
        fire(age, 'blur');                              // ... and show the age message straight away
      }
    }
    nic.addEventListener('input', function () { editing = false; run(); });
    nic.addEventListener('change', run);
    if (edit) edit.addEventListener('click', function () { editing = true; showFields(true); sex.focus(); });
    run();
  });

  /* ------------------------------------------------ inline validation */
  (function () {
    var forms = document.querySelectorAll('form[data-validate]');
    if (!forms.length) return;
    var EMAIL = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$/;
    var PHONE = /^\+?[0-9 \-]{9,16}$/;
    var NIC = /^([0-9]{9}[VvXx]|[0-9]{12})$/;
    var LICENCE = /^[A-Za-z0-9-]{6,15}$/;
    // every rule returns '' (fine) or the message to show; the wording mirrors the server's messages
    var RULES = {
      required: function (v, el, form, arg) { return (el.type === 'checkbox' ? el.checked : String(v).trim()) ? '' : (arg || 'This field is required'); },
      check: function (v, el, form, arg) { return el.checked ? '' : (arg || 'Please tick this box to continue'); },
      name: function (v) { if (!v.trim()) return 'Please enter your full name'; return v.length < 2 || v.length > 80 ? 'Name must be 2 - 80 characters' : ''; },
      email: function (v) { if (!v.trim()) return 'Please enter your email'; return EMAIL.test(v) ? '' : 'Enter a valid email address, e.g. name@example.com'; },
      phone: function (v) { if (!v.trim()) return 'Please enter your phone number'; return PHONE.test(v) ? '' : 'Enter a valid phone number, e.g. 0771234567'; },
      address: function (v) { return v.length > 160 ? 'Address is too long' : ''; },
      password: function (v) {
        if (!v) return 'Please choose a password';
        if (v.length < 8) return 'Password must be at least 8 characters';
        if (v.length > 64) return 'Password must be 64 characters or fewer';
        return /[A-Za-z]/.test(v) && /\d/.test(v) ? '' : 'Password needs at least one letter and one digit';
      },
      match: function (v, el, form, arg) {
        var other = document.getElementById(arg);
        if (!v) return 'Please confirm your password';
        return other && other.value === v ? '' : 'The two passwords do not match';
      },
      nic: function (v) {
        if (!v.trim()) return 'Please enter your NIC number';
        return NIC.test(v.trim()) ? '' : 'NIC must be 9 digits + V/X, or 12 digits';
      },
      licence: function (v, el, form) {
        var drv = form.querySelector('[name="withDriver"]:checked');
        var selfDrive = !drv || drv.value !== 'true';
        if (!v.trim()) return selfDrive ? 'Enter your driving licence ID for self drive, or choose "With a driver"' : '';
        return LICENCE.test(v.trim()) ? '' : 'Licence ID must be 6 - 15 letters / digits';
      },
      age: function (v) {
        if (v === '' || v == null) return 'Please enter your age';
        var n = Number(v);
        if (!isFinite(n) || n !== Math.floor(n)) return 'Enter your age as a whole number';
        if (n < 18) return 'Renters must be 18 or older';
        return n > 80 ? 'Renters must be 80 or younger' : '';
      }
    };

    function check(el, form) {
      var rule = el.getAttribute('data-rule');
      if (!rule) return '';
      var i = rule.indexOf(':'), name = i < 0 ? rule : rule.slice(0, i), arg = i < 0 ? '' : rule.slice(i + 1);
      var fn = RULES[name];
      return fn ? fn(el.value, el, form, arg) : '';
    }
    function show(el, msg) {
      var field = el.closest('.field') || el.parentNode;
      var err = field.querySelector('.field-error');
      if (!err) {
        err = document.createElement('div');
        err.className = 'field-error';
        err.id = (el.id || 'f' + Math.random().toString(36).slice(2, 7)) + '-error';
        field.appendChild(err);
      }
      if (msg) {
        err.textContent = msg; err.hidden = false;
        field.classList.add('invalid'); field.classList.remove('valid');
        el.setAttribute('aria-invalid', 'true');
        if (err.id) el.setAttribute('aria-describedby', err.id);
      } else {
        err.textContent = ''; err.hidden = true;
        field.classList.remove('invalid');
        if (String(el.value).trim() || el.checked) field.classList.add('valid'); else field.classList.remove('valid');
        el.removeAttribute('aria-invalid');
      }
    }

    forms.forEach(function (form) {
      var touched = new WeakSet();
      var inputs = Array.prototype.slice.call(form.querySelectorAll('[data-rule]'));
      function validate(el) { var m = check(el, form); show(el, m); return m; }
      inputs.forEach(function (el) {
        el.addEventListener('blur', function () { touched.add(el); validate(el); });
        var live = function () {
          var field = el.closest('.field');
          if (touched.has(el) || (field && field.classList.contains('invalid'))) validate(el);
          // typing in the password also re-checks "confirm password"
          inputs.forEach(function (o) { var r = o.getAttribute('data-rule') || ''; if (r === 'match:' + el.id && touched.has(o)) validate(o); });
        };
        el.addEventListener('input', live);
        el.addEventListener('change', live);
      });
      form.querySelectorAll('[name="withDriver"]').forEach(function (r) {   // switching to self drive makes the licence mandatory
        r.addEventListener('change', function () {
          inputs.forEach(function (o) { if (o.getAttribute('data-rule') === 'licence' && touched.has(o)) validate(o); });
        });
      });
      form.addEventListener('submit', function (e) {
        var first = null;
        inputs.forEach(function (el) {
          if (el.disabled || el.type === 'hidden') return;
          var field = el.closest('.field');
          if (field && field.offsetParent === null) return;                   // hidden (derived or not applicable)
          touched.add(el);
          if (validate(el) && !first) first = el;
        });
        if (first) {
          e.preventDefault();
          first.focus();
          if (first.scrollIntoView) first.scrollIntoView({ block: 'center', behavior: reduceMotion ? 'auto' : 'smooth' });
        }
      });
    });
  })();

  /* ------------------------------------------------ mini availability calendar */
  document.querySelectorAll('[data-calendar]').forEach(function (cal) {
    var form = cal.closest('form');
    var pickup = form && form.querySelector('[data-pickup]'), ret = form && form.querySelector('[data-return]');
    if (!pickup || !ret) return;
    var vehicleId = cal.getAttribute('data-vehicle-id');
    var MAX_DAYS = 60, MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    var DOW = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    var booked = {}, state = 'loading', today = startOfToday();
    var firstSel = parseIso(pickup.value);
    var view = new Date((firstSel && firstSel >= today ? firstSel : today).getFullYear(), (firstSel && firstSel >= today ? firstSel : today).getMonth(), 1);
    var lastMonthLimit = new Date(today.getFullYear(), today.getMonth() + 6, 1);
    var note = '';

    function addD(d, n) { var x = new Date(d.getFullYear(), d.getMonth(), d.getDate() + n); return x; }
    function label(d) { return d.getDate() + ' ' + MONTHS[d.getMonth()].slice(0, 3); }
    function freeBetween(startIso, endIso) {                       // all nights from start up to (not including) end are free
      for (var d = parseIso(startIso), end = parseIso(endIso); d < end; d = addD(d, 1)) { if (booked[isoDate(d)]) return false; }
      return true;
    }
    function status() {
      var s = pickup.value, r = ret.value;
      if (note) return note;
      if (!s) return 'Choose your pickup day';
      if (!r) return 'Pickup ' + label(parseIso(s)) + '. Now choose your return day';
      var days = Math.round((parseIso(r) - parseIso(s)) / 86400000);
      return label(parseIso(s)) + ' to ' + label(parseIso(r)) + ' (' + days + ' day' + (days === 1 ? '' : 's') + ')';
    }

    function render(focusIso) {
      var s = pickup.value, r = ret.value, y = view.getFullYear(), m = view.getMonth();
      var html = '<div class="cal-head"><button type="button" class="cal-nav" data-nav="-1" aria-label="Previous month"' +
        (view <= new Date(today.getFullYear(), today.getMonth(), 1) ? ' disabled' : '') + '>&lsaquo;</button>' +
        '<strong class="cal-title" aria-live="polite">' + MONTHS[m] + ' ' + y + '</strong>' +
        '<button type="button" class="cal-nav" data-nav="1" aria-label="Next month"' + (addMonth(view, 1) >= lastMonthLimit ? ' disabled' : '') + '>&rsaquo;</button></div>';
      html += '<div class="cal-grid" role="grid">';
      DOW.forEach(function (d) { html += '<span class="cal-dow" aria-hidden="true">' + d + '</span>'; });
      var first = new Date(y, m, 1), offset = (first.getDay() + 6) % 7, daysInMonth = new Date(y, m + 1, 0).getDate();
      for (var i = 0; i < offset; i++) html += '<span class="cal-pad"></span>';
      for (var day = 1; day <= daysInMonth; day++) {
        var d = new Date(y, m, day), iso = isoDate(d), cls = 'cal-day', blocked = false, why = '';
        if (d < today) { cls += ' is-past'; blocked = true; why = 'in the past'; }
        else if (s && !r) {                                        // choosing the return day
          if (iso > s) {
            var tooLong = Math.round((d - parseIso(s)) / 86400000) > MAX_DAYS;
            if (tooLong || !freeBetween(s, iso)) { blocked = true; why = tooLong ? 'too long' : 'not available'; if (booked[iso]) cls += ' is-booked'; }
          } else if (booked[iso]) { cls += ' is-booked'; blocked = true; why = 'booked'; }
        } else if (booked[iso]) { cls += ' is-booked'; blocked = true; why = 'booked'; }
        if (iso === isoDate(today)) cls += ' is-today';
        if (s && iso === s) cls += ' range-start';
        if (r && iso === r) cls += ' range-end';
        if (s && r && iso > s && iso < r) cls += ' in-range';
        var aria = d.toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }) + (why ? ', ' + why : ', available');
        html += '<button type="button" class="' + cls + '" data-date="' + iso + '" aria-label="' + aria + '"' + (blocked ? ' aria-disabled="true"' : '') +
          (s && iso === s || r && iso === r ? ' aria-pressed="true"' : '') + '>' + day + '</button>';
      }
      html += '</div><p class="cal-status" aria-live="polite">' + status() + '</p>';
      html += '<div class="cal-legend"><span><i class="lg free"></i>Free</span><span><i class="lg booked"></i>Booked</span><span><i class="lg pick"></i>Your dates</span></div>';
      if (state === 'error') html += '<p class="hint">Could not load booked days. The availability check below still works.</p>';
      cal.innerHTML = html;
      cal.classList.toggle('is-loading', state === 'loading');
      if (focusIso) { var b = cal.querySelector('[data-date="' + focusIso + '"]'); if (b) b.focus(); }
    }
    function addMonth(d, n) { return new Date(d.getFullYear(), d.getMonth() + n, 1); }

    function setVal(el, v) { if (el.value !== v) { el.value = v; fire(el, 'change'); } }
    function pick(iso) {
      var s = pickup.value, r = ret.value; note = '';
      if (!s || r || iso <= s) {
        if (booked[iso]) return;
        setVal(ret, ''); setVal(pickup, iso);
      } else {
        if (!freeBetween(s, iso)) { note = 'Those dates include booked days. Choose another return day.'; render(); return; }
        setVal(ret, iso);
      }
      render(iso);
    }

    cal.addEventListener('click', function (e) {
      var nav = e.target.closest('[data-nav]');
      if (nav && !nav.disabled) { view = addMonth(view, +nav.getAttribute('data-nav')); render(); return; }
      var b = e.target.closest('[data-date]');
      if (!b) return;
      if (b.getAttribute('aria-disabled') === 'true') { note = b.getAttribute('aria-label').indexOf('past') > -1 ? '' : 'That day is not available.'; render(b.getAttribute('data-date')); return; }
      pick(b.getAttribute('data-date'));
    });
    cal.addEventListener('keydown', function (e) {                 // arrow keys move between days
      var b = e.target.closest && e.target.closest('[data-date]');
      if (!b) return;
      var step = { ArrowLeft: -1, ArrowRight: 1, ArrowUp: -7, ArrowDown: 7 }[e.key];
      if (!step) return;
      e.preventDefault();
      var target = addD(parseIso(b.getAttribute('data-date')), step);
      if (target < today) return;
      if (target.getMonth() !== view.getMonth() || target.getFullYear() !== view.getFullYear()) view = new Date(target.getFullYear(), target.getMonth(), 1);
      render(isoDate(target));
    });
    function syncFromInputs(e) {                                   // user typed in the date boxes
      if (e && e.isTrusted === false) return;                      // our own events
      var d = parseIso(pickup.value);
      if (d && d >= today) view = new Date(d.getFullYear(), d.getMonth(), 1);
      note = ''; render();
    }
    pickup.addEventListener('change', syncFromInputs);
    ret.addEventListener('change', syncFromInputs);

    render();
    fetch('/api/vehicles/' + vehicleId + '/calendar?from=' + isoDate(today) + '&days=210', { headers: { 'Accept': 'application/json' } })
      .then(function (r) { return r.json(); })
      .then(function (data) {
        (data.booked || []).forEach(function (d) { booked[d] = true; });
        state = 'ready'; render();
      })
      .catch(function () { state = 'error'; render(); });
  });

  /* ------------------------------------------------ phone tab bar (bottom navigation) */
  (function () {
    var nav = document.querySelector('.nav'), links = document.querySelector('.nav-links');
    if (!nav || !links || document.querySelector('.sidebar') || document.querySelector('.tabbar')) return;
    function find(re) { return Array.prototype.slice.call(links.querySelectorAll('a')).filter(function (a) { return re.test(a.getAttribute('href') || ''); })[0]; }
    var customer = !!find(/\/bookings$/);
    var wanted = customer ? [/^\/$|^\/?$/, /\/vehicles$/, /\/bookings$/, /\/profile$/] : [/^\/$|^\/?$/, /\/vehicles$/, /\/faq$/, /\/login$/];
    var picked = [];
    wanted.forEach(function (re) {
      var a = Array.prototype.slice.call(links.querySelectorAll('a')).filter(function (x) {
        var h = x.getAttribute('href') || '';
        var path = h.replace(/^https?:\/\/[^/]+/, '').split('?')[0];
        return re.test(path || '/') && !x.classList.contains('btn');
      })[0];
      if (!a && re.source.indexOf('login') > -1) a = find(/\/login/);
      if (a) picked.push(a);
    });
    if (picked.length < 3) return;
    var bar = document.createElement('nav');
    bar.className = 'tabbar'; bar.setAttribute('aria-label', 'Main');
    var html = '';
    picked.forEach(function (a) {
      var icon = a.querySelector('svg') ? a.querySelector('svg').outerHTML : '';
      var label = a.textContent.replace(/\s+/g, ' ').trim();
      html += '<a href="' + a.getAttribute('href') + '"' + (a.classList.contains('active') ? ' class="active" aria-current="page"' : '') + '>' + icon + '<span>' + label + '</span></a>';
    });
    html += '<button type="button" class="tab-more" aria-haspopup="dialog" aria-expanded="false"><svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><circle cx="5" cy="12" r="1.6"/><circle cx="12" cy="12" r="1.6"/><circle cx="19" cy="12" r="1.6"/></svg><span>More</span></button>';
    bar.innerHTML = html;
    document.body.appendChild(bar);
    document.body.classList.add('has-tabbar');

    var sheet = document.createElement('div');
    sheet.className = 'tab-sheet'; sheet.hidden = true; sheet.setAttribute('role', 'dialog'); sheet.setAttribute('aria-label', 'More');
    var pickedHrefs = picked.map(function (a) { return a.getAttribute('href'); });
    var sh = '<div class="tab-sheet-card"><div class="sheet-grab" aria-hidden="true"></div>', hasFaq = pickedHrefs.some(function (h) { return /\/faq$/.test(h); });
    Array.prototype.slice.call(links.querySelectorAll('a')).forEach(function (a) {
      if (pickedHrefs.indexOf(a.getAttribute('href')) > -1) return;
      if (/\/faq$/.test(a.getAttribute('href') || '')) hasFaq = true;
      var icon = a.querySelector('svg') ? a.querySelector('svg').outerHTML : '';
      var label = a.classList.contains('bell') ? 'Notifications' : a.textContent.replace(/\s+/g, ' ').trim();
      var badge = a.querySelector('.bell-badge') ? '<span class="side-badge">' + a.querySelector('.bell-badge').textContent + '</span>' : '';
      sh += '<a href="' + a.getAttribute('href') + '" class="sheet-link">' + icon + '<span>' + label + '</span>' + badge + '</a>';
    });
    if (!hasFaq) sh += '<a href="/faq" class="sheet-link"><svg class="ico" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M9.5 9.5a2.5 2.5 0 115 0c0 1.5-2.5 2-2.5 3.5M12 17h.01"/></svg><span>Help and FAQ</span></a>';
    sh += '</div>';
    sheet.innerHTML = sh;
    document.body.appendChild(sheet);
    var more = bar.querySelector('.tab-more');
    function toggleSheet(open) {
      sheet.hidden = !open; more.setAttribute('aria-expanded', open ? 'true' : 'false');
      document.body.classList.toggle('sheet-open', open);
      if (open) { var f = sheet.querySelector('a'); if (f) f.focus(); } else more.focus();
    }
    more.addEventListener('click', function () { toggleSheet(sheet.hidden); });
    sheet.addEventListener('click', function (e) { if (e.target === sheet) toggleSheet(false); });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape' && !sheet.hidden) toggleSheet(false); });
  })();

  /* ------------------------------------------------ sortable columns + CSV export */
  (function () {
    function cellText(td) { return (td.getAttribute('data-sort') || td.textContent).replace(/\s+/g, ' ').trim(); }
    var MONTHS = { jan: 0, feb: 1, mar: 2, apr: 3, may: 4, jun: 5, jul: 6, aug: 7, sep: 8, oct: 9, nov: 10, dec: 11 };
    function keyOf(text, kind) {
      if (kind === 'date') {
        var m = /(\d{1,2}) ([A-Za-z]{3}) (\d{4})/.exec(text);
        return m ? new Date(+m[3], MONTHS[m[2].toLowerCase()], +m[1]).getTime() : -Infinity;
      }
      if (kind === 'number') { var n = parseFloat(text.replace(/[^0-9.\-]/g, '')); return isNaN(n) ? -Infinity : n; }
      return text.toLowerCase();
    }
    function kindOf(rows, col) {
      var texts = rows.map(function (r) { return r.cells[col] ? cellText(r.cells[col]) : ''; }).filter(Boolean);
      if (!texts.length) return 'text';
      if (texts.every(function (t) { return /\d{1,2} [A-Za-z]{3} \d{4}/.test(t); })) return 'date';
      if (texts.every(function (t) { return /^(LKR\s*)?-?[\d,]+(\.\d+)?$/.test(t); })) return 'number';
      return 'text';
    }
    function skipColumn(th) { var t = th.textContent.trim().toLowerCase(); return th.hasAttribute('data-no-sort') || t === '' || t === 'actions' || t === 'action' || t === 'receipt'; }

    document.querySelectorAll('.admin-main table, table[data-sortable]').forEach(function (table) {
      if (table.hasAttribute('data-no-sort') || !table.tHead || !table.tBodies[0]) return;
      var heads = Array.prototype.slice.call(table.tHead.rows[0].cells);
      heads.forEach(function (th, col) {
        if (skipColumn(th)) return;
        th.classList.add('sortable'); th.tabIndex = 0; th.setAttribute('role', 'columnheader'); th.setAttribute('aria-sort', 'none');
        function sort() {
          var dir = th.getAttribute('aria-sort') === 'ascending' ? 'descending' : 'ascending';
          heads.forEach(function (h) { if (h.classList.contains('sortable')) h.setAttribute('aria-sort', 'none'); });
          th.setAttribute('aria-sort', dir);
          var body = table.tBodies[0], rows = Array.prototype.slice.call(body.rows);
          var kind = kindOf(rows, col), sign = dir === 'ascending' ? 1 : -1;
          var keyed = rows.map(function (r, i) { return { r: r, i: i, k: keyOf(r.cells[col] ? cellText(r.cells[col]) : '', kind) }; });
          keyed.sort(function (a, b) { return (a.k < b.k ? -1 : a.k > b.k ? 1 : a.i - b.i) * (a.k === b.k ? 1 : sign); });
          keyed.forEach(function (x) { body.appendChild(x.r); });
          if (table._pager) table._pager.refresh();
        }
        th.addEventListener('click', sort);
        th.addEventListener('keydown', function (e) { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); sort(); } });
      });
    });

    function csvCell(v) { v = String(v).replace(/\s+/g, ' ').trim(); return /[",\n]/.test(v) ? '"' + v.replace(/"/g, '""') + '"' : v; }
    document.querySelectorAll('table[data-export]').forEach(function (table) {
      var host = table.closest('.table-wrap') || table;
      var tools = document.createElement('div');
      tools.className = 'table-tools';
      tools.innerHTML = '<button type="button" class="btn small ghost">Export CSV</button>';
      host.parentNode.insertBefore(tools, host);
      tools.querySelector('button').addEventListener('click', function () {
        var heads = Array.prototype.slice.call(table.tHead.rows[0].cells), keep = [];
        heads.forEach(function (th, i) { if (!skipColumn(th) || th.hasAttribute('data-csv')) keep.push(i); });
        var lines = [keep.map(function (i) { return csvCell(heads[i].textContent); }).join(',')];
        Array.prototype.slice.call(table.tBodies[0].rows).forEach(function (r) {
          if (r.cells.length < heads.length) return;                          // "nothing found" row
          lines.push(keep.map(function (i) { return csvCell(cellText(r.cells[i])); }).join(','));
        });
        var blob = new Blob(['\uFEFF' + lines.join('\r\n')], { type: 'text/csv;charset=utf-8' });
        var a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = (table.getAttribute('data-export') || 'export') + '-' + isoDate(new Date()) + '.csv';
        a.setAttribute('data-no-loader', '');
        document.body.appendChild(a); a.click(); a.remove();
        window.setTimeout(function () { URL.revokeObjectURL(a.href); }, 2000);
      });
    });
  })();

  /* ------------------------------------------------ admin sidebar counts */
  (function () {
    var badges = document.querySelectorAll('[data-count]');
    if (!badges.length) return;
    function load() {
      fetch('/admin/api/counts', { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' })
        .then(function (r) { return r.ok ? r.json() : null; })
        .then(function (c) {
          if (!c) return;
          badges.forEach(function (b) {
            var n = Number(c[b.getAttribute('data-count')] || 0);
            b.textContent = n > 99 ? '99+' : String(n);
            b.hidden = n < 1;
            var tip = b.getAttribute('data-tip');
            if (tip) b.title = n + ' ' + tip;
          });
        })
        .catch(function () { /* the badges simply stay hidden */ });
    }
    load();
    window.setInterval(load, 60000);
  })();

  /* ------------------------------------------------ bell dropdown (desktop) */
  (function () {
    var bell = document.querySelector('a.bell[data-bell]');
    if (!bell || bell.classList.contains('active') || !window.matchMedia) return;
    var wide = window.matchMedia('(min-width: 946px)');
    var panel = null, open = false;
    var listUrl = bell.getAttribute('href');
    var isAdmin = listUrl.indexOf('/admin') > -1;
    var readAll = isAdmin ? '/admin/notifications/read-all' : '/notifications/read-all';
    bell.setAttribute('aria-haspopup', 'dialog'); bell.setAttribute('aria-expanded', 'false');

    function build() {
      panel = document.createElement('div');
      panel.className = 'bell-panel'; panel.hidden = true; panel.setAttribute('role', 'dialog'); panel.setAttribute('aria-label', 'Notifications');
      panel.innerHTML = '<div class="bp-head"><strong>Notifications</strong><button type="button" class="linklike" data-readall>Mark all as read</button></div>' +
        '<div class="bp-list" aria-live="polite"><p class="muted small bp-empty">Loading...</p></div>' +
        '<a class="bp-foot" href="' + listUrl + '">View all notifications</a>';
      document.body.appendChild(panel);
      panel.querySelector('[data-readall]').addEventListener('click', function () {
        fetch(readAll, { method: 'POST', redirect: 'manual', credentials: 'same-origin' }).finally(function () {
          panel.querySelectorAll('.bp-item.unread').forEach(function (i) { i.classList.remove('unread'); });
          var badge = bell.querySelector('.bell-badge'); if (badge) badge.remove();
        });
      });
    }
    function place() {
      var r = bell.getBoundingClientRect(), w = Math.min(360, window.innerWidth - 16);
      panel.style.width = w + 'px';
      panel.style.top = (r.bottom + 10) + 'px';
      panel.style.left = Math.max(8, Math.min(window.innerWidth - w - 8, r.right - w)) + 'px';
    }
    function fill(data) {
      var list = panel.querySelector('.bp-list');
      if (!data.items || !data.items.length) { list.innerHTML = '<p class="muted small bp-empty">You are all caught up.</p>'; return; }
      list.innerHTML = data.items.map(function (n) {
        return '<a class="bp-item' + (n.read ? '' : ' unread') + '" href="' + esc(n.url) + '"><span class="dot" aria-hidden="true"></span><div>' +
          '<span class="badge ' + esc(n.css) + '">' + esc(n.type) + '</span><strong>' + esc(n.title) + '</strong><p>' + esc(n.message) + '</p></div><time>' + esc(n.when) + '</time></a>';
      }).join('');
    }
    function setOpen(on) {
      if (!panel) build();
      open = on; panel.hidden = !on; bell.setAttribute('aria-expanded', on ? 'true' : 'false');
      if (on) {
        place();
        fetch('/api/notifications/recent', { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' })
          .then(function (r) { return r.json(); }).then(fill)
          .catch(function () { panel.querySelector('.bp-list').innerHTML = '<p class="muted small bp-empty">Could not load notifications. <a href="' + listUrl + '">Open the full list</a>.</p>'; });
      }
    }
    bell.addEventListener('click', function (e) {
      if (!wide.matches || e.metaKey || e.ctrlKey || e.shiftKey || e.button !== 0) return;     // phones and new-tab clicks use the page
      e.preventDefault(); e.stopPropagation();
      setOpen(!open);
    }, true);
    document.addEventListener('click', function (e) { if (open && !panel.contains(e.target) && !bell.contains(e.target)) setOpen(false); });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape' && open) { setOpen(false); bell.focus(); } });
    window.addEventListener('resize', function () { if (open) { if (!wide.matches) setOpen(false); else place(); } });
  })();

  /* ------------------------------------------------ FAQ search + expand all */
  (function () {
    var search = document.querySelector('[data-faq-search]');
    if (!search) return;
    var items = Array.prototype.slice.call(document.querySelectorAll('[data-faq] details'));
    var groups = Array.prototype.slice.call(document.querySelectorAll('[data-faq-group]'));
    var count = document.querySelector('[data-faq-count]'), empty = document.querySelector('[data-faq-empty]');
    function apply() {
      var q = search.value.trim().toLowerCase(), shown = 0;
      items.forEach(function (d) {
        var hit = !q || d.textContent.toLowerCase().indexOf(q) > -1;
        d.hidden = !hit; if (hit) shown++;
        if (q && hit) d.open = true;
      });
      groups.forEach(function (g) { g.hidden = !g.querySelector('details:not([hidden])'); });
      if (count) count.textContent = q ? shown + ' answer' + (shown === 1 ? '' : 's') + ' found' : '';
      if (empty) empty.hidden = shown > 0;
    }
    search.addEventListener('input', apply);
    var toggle = document.querySelector('[data-faq-toggle]');
    if (toggle) toggle.addEventListener('click', function () {
      var openAll = toggle.getAttribute('data-state') !== 'open';
      items.forEach(function (d) { if (!d.hidden) d.open = openAll; });
      toggle.setAttribute('data-state', openAll ? 'open' : 'closed');
      toggle.textContent = openAll ? 'Collapse all' : 'Expand all';
    });
    if (location.hash) { var t = document.getElementById(location.hash.slice(1)); if (t && t.tagName === 'DETAILS') t.open = true; }
  })();
})();
