<?php
/* =========================================================
   welcome.php  —  صفحه ورود به اپ
   مسیر: public_html/panel/app/build/welcome.php
   =========================================================
   📌 این صفحه قبل از WebView نمایش داده می‌شه
   📌 آیکون اختیاری — اگه خالی باشه نمایش داده نمی‌شه
   ========================================================= */

// ===== مقادیر پیش‌فرض =====
$welcome = [
  'enabled'          => 1,
  'title'            => '',
  'subtitle'         => '',
  'icon'             => '',
  'button_text'      => 'ورود',
  'button_bg'        => '#ec4899',
  'button_text_color'=> '#ffffff',
  'bg_color'         => '#1f0a1c',
  'title_color'      => '#fdf2f8',
  'subtitle_color'   => '#fbcfe8',
];

// ===== بازخوانی از data.json =====
if (isset($saved['welcome']) && is_array($saved['welcome'])) {
  $welcome = array_merge($welcome, $saved['welcome']);
}
?>

<!-- ===== کارت ۱: تنظیمات عمومی ===== -->
<div class="card">

  <div class="card-head">
    <div>
      <div class="card-title">🚪 صفحه ورود</div>
      <div class="card-subtitle">صفحه‌ای که قبل از وب‌ویوئر نمایش داده می‌شه</div>
    </div>
  </div>

  <div class="field" style="margin-bottom:0;">
    <label class="field-label">فعال بودن صفحه</label>
    <label style="display:flex; align-items:center; gap:8px; padding:10px 12px; background:var(--bg-soft); border-radius:var(--r-md); cursor:pointer;">
      <input type="checkbox" id="welcome_enabled" name="welcome_enabled" value="1" <?= !empty($welcome['enabled']) ? 'checked' : '' ?> style="width:18px; height:18px; accent-color:var(--accent);">
      <span style="font-size:0.85rem; font-weight:600; color:var(--text);">نمایش داده بشه</span>
    </label>
  </div>

</div>

<!-- ===== کارت ۲: آیکون و متن ===== -->
<div class="card" style="margin-top:16px;">

  <div class="card-head">
    <div>
      <div class="card-title">🎨 آیکون و متن</div>
      <div class="card-subtitle">همه اختیاری — اگه خالی بذاری نمایش داده نمی‌شن</div>
    </div>
  </div>

  <div class="field">
    <label class="field-label" for="welcome_icon">آیکون (ایموجی)</label>
    <input type="text" id="welcome_icon" name="welcome_icon" class="field-input" placeholder="🚀" value="<?= htmlspecialchars($welcome['icon']) ?>" maxlength="10" style="height:42px; font-size:1.4rem; text-align:center;">
    <small class="field-hint" style="display:block; margin-top:4px; font-size:0.75rem; color:var(--text-muted);">
      خالی بذار = بدون آیکون
    </small>
  </div>

  <div class="field" style="margin-top:14px;">
    <label class="field-label" for="welcome_title">تیتر</label>
    <input type="text" id="welcome_title" name="welcome_title" class="field-input" placeholder="خالی بذار = بدون تیتر" value="<?= htmlspecialchars($welcome['title']) ?>" style="height:42px;">
  </div>

  <div class="field" style="margin-top:14px;">
    <label class="field-label" for="welcome_subtitle">زیرتیتر</label>
    <textarea id="welcome_subtitle" name="welcome_subtitle" class="field-textarea" rows="2" style="min-height:64px;" placeholder="خالی بذار = بدون زیرتیتر"><?= htmlspecialchars($welcome['subtitle']) ?></textarea>
  </div>

</div>

<!-- ===== کارت ۳: دکمه ===== -->
<div class="card" style="margin-top:16px;">

  <div class="card-head">
    <div>
      <div class="card-title">🔘 دکمه ورود</div>
      <div class="card-subtitle">متن و رنگ دکمه</div>
    </div>
  </div>

  <div class="field">
    <label class="field-label" for="welcome_button_text">متن دکمه</label>
    <input type="text" id="welcome_button_text" name="welcome_button_text" class="field-input" value="<?= htmlspecialchars($welcome['button_text']) ?>" style="height:42px;">
  </div>

  <div style="display:grid; grid-template-columns:1fr 1fr; gap:14px; margin-top:14px;">

    <div class="field" style="margin-bottom:0;">
      <label class="field-label">رنگ دکمه</label>
      <div style="display:grid; grid-template-columns:56px 1fr; gap:8px; align-items:center;">
        <input type="color" class="welcome-picker" data-target="welcome_button_bg" value="<?= htmlspecialchars($welcome['button_bg']) ?>" style="width:56px; height:42px; border:1px solid var(--border); border-radius:var(--r-md); cursor:pointer; padding:2px;">
        <input type="text" id="welcome_button_bg" name="welcome_button_bg" class="field-input" value="<?= htmlspecialchars($welcome['button_bg']) ?>" maxlength="7" dir="ltr" style="height:42px; font-family:'Courier New', monospace;">
      </div>
    </div>

    <div class="field" style="margin-bottom:0;">
      <label class="field-label">رنگ متن دکمه</label>
      <div style="display:grid; grid-template-columns:56px 1fr; gap:8px; align-items:center;">
        <input type="color" class="welcome-picker" data-target="welcome_button_text_color" value="<?= htmlspecialchars($welcome['button_text_color']) ?>" style="width:56px; height:42px; border:1px solid var(--border); border-radius:var(--r-md); cursor:pointer; padding:2px;">
        <input type="text" id="welcome_button_text_color" name="welcome_button_text_color" class="field-input" value="<?= htmlspecialchars($welcome['button_text_color']) ?>" maxlength="7" dir="ltr" style="height:42px; font-family:'Courier New', monospace;">
      </div>
    </div>

  </div>

</div>

<!-- ===== کارت ۴: رنگ‌ها ===== -->
<div class="card" style="margin-top:16px;">

  <div class="card-head">
    <div>
      <div class="card-title">🎨 رنگ‌ها</div>
      <div class="card-subtitle">پس‌زمینه و رنگ متن‌ها</div>
    </div>
  </div>

  <div style="display:grid; grid-template-columns:1fr 1fr 1fr; gap:14px;">

    <div class="field" style="margin-bottom:0;">
      <label class="field-label">رنگ پس‌زمینه</label>
      <div style="display:grid; grid-template-columns:56px 1fr; gap:8px; align-items:center;">
        <input type="color" class="welcome-picker" data-target="welcome_bg_color" value="<?= htmlspecialchars($welcome['bg_color']) ?>" style="width:56px; height:42px; border:1px solid var(--border); border-radius:var(--r-md); cursor:pointer; padding:2px;">
        <input type="text" id="welcome_bg_color" name="welcome_bg_color" class="field-input" value="<?= htmlspecialchars($welcome['bg_color']) ?>" maxlength="7" dir="ltr" style="height:42px; font-family:'Courier New', monospace;">
      </div>
    </div>

    <div class="field" style="margin-bottom:0;">
      <label class="field-label">رنگ تیتر</label>
      <div style="display:grid; grid-template-columns:56px 1fr; gap:8px; align-items:center;">
        <input type="color" class="welcome-picker" data-target="welcome_title_color" value="<?= htmlspecialchars($welcome['title_color']) ?>" style="width:56px; height:42px; border:1px solid var(--border); border-radius:var(--r-md); cursor:pointer; padding:2px;">
        <input type="text" id="welcome_title_color" name="welcome_title_color" class="field-input" value="<?= htmlspecialchars($welcome['title_color']) ?>" maxlength="7" dir="ltr" style="height:42px; font-family:'Courier New', monospace;">
      </div>
    </div>

    <div class="field" style="margin-bottom:0;">
      <label class="field-label">رنگ زیرتیتر</label>
      <div style="display:grid; grid-template-columns:56px 1fr; gap:8px; align-items:center;">
        <input type="color" class="welcome-picker" data-target="welcome_subtitle_color" value="<?= htmlspecialchars($welcome['subtitle_color']) ?>" style="width:56px; height:42px; border:1px solid var(--border); border-radius:var(--r-md); cursor:pointer; padding:2px;">
        <input type="text" id="welcome_subtitle_color" name="welcome_subtitle_color" class="field-input" value="<?= htmlspecialchars($welcome['subtitle_color']) ?>" maxlength="7" dir="ltr" style="height:42px; font-family:'Courier New', monospace;">
      </div>
    </div>

  </div>

</div>

<!-- ===== کارت ۵: پیش‌نمایش زنده ===== -->
<div class="card" style="margin-top:16px;">

  <div class="card-head">
    <div>
      <div class="card-title">👁 پیش‌نمایش زنده</div>
      <div class="card-subtitle">صفحه‌ی ورود رو ببین</div>
    </div>
  </div>

  <div style="display:flex; justify-content:center; padding:16px; background:var(--bg-soft); border-radius:var(--r-lg);">

    <div
      id="welcomePreviewPhone"
      style="width:220px; aspect-ratio:9/16; border-radius:20px; border:6px solid #222; overflow:hidden; box-shadow:var(--shadow-lg); position:relative; display:flex; flex-direction:column;"
    >

      <div
        id="welcomePreviewInner"
        style="width:100%; height:100%; display:flex; flex-direction:column; align-items:center; justify-content:center; gap:16px; text-align:center; padding:24px 18px;"
      >

        <div id="welcomePreviewIcon" style="font-size:4rem; line-height:1; display:none;">🚀</div>

        <div id="welcomePreviewTitle" style="font-size:1.1rem; font-weight:800; line-height:1.3; display:none;">عنوان</div>

        <div id="welcomePreviewSubtitle" style="font-size:0.82rem; font-weight:500; line-height:1.7; opacity:0.9; display:none;">زیرعنوان</div>

        <div id="welcomePreviewBtn" style="width:100%; padding:14px; border-radius:12px; font-size:0.9rem; font-weight:800; text-align:center; margin-top:8px;">ورود</div>

      </div>

    </div>

  </div>

</div>

<script>
/* =========================================================
   sync رنگ‌ها
   ========================================================= */
(function () {
  document.querySelectorAll('.welcome-picker').forEach(function (picker) {
    var targetId = picker.getAttribute('data-target');
    if (!targetId) return;
    var textInput = document.getElementById(targetId);
    if (!textInput) return;

    picker.addEventListener('input', function () {
      textInput.value = this.value;
      updateWelcomePreview();
    });

    textInput.addEventListener('input', function () {
      var val = this.value.trim();
      if (/^#[0-9A-Fa-f]{6}$/.test(val)) {
        picker.value = val;
        updateWelcomePreview();
      }
    });
  });
})();

/* =========================================================
   پیش‌نمایش زنده
   ========================================================= */
function updateWelcomePreview() {
  var phone    = document.getElementById('welcomePreviewPhone');
  var iconEl   = document.getElementById('welcomePreviewIcon');
  var titleEl  = document.getElementById('welcomePreviewTitle');
  var subEl    = document.getElementById('welcomePreviewSubtitle');
  var btnEl    = document.getElementById('welcomePreviewBtn');

  if (!phone) return;

  // ===== متن‌ها =====
  var iconInput     = document.getElementById('welcome_icon');
  var titleInput    = document.getElementById('welcome_title');
  var subtitleInput = document.getElementById('welcome_subtitle');
  var btnTextInput  = document.getElementById('welcome_button_text');

  // ===== رنگ‌ها =====
  var bgColor       = document.getElementById('welcome_bg_color');
  var titleColor    = document.getElementById('welcome_title_color');
  var subtitleColor = document.getElementById('welcome_subtitle_color');
  var btnBg         = document.getElementById('welcome_button_bg');
  var btnTextColor  = document.getElementById('welcome_button_text_color');

  // ===== اعمال =====
  phone.style.background = bgColor ? bgColor.value : '#1f0a1c';

  // ===== آیکون =====
  if (iconInput && iconInput.value.trim() !== '') {
    iconEl.textContent = iconInput.value;
    iconEl.style.display = 'block';
  } else {
    iconEl.style.display = 'none';
  }

  // ===== تیتر =====
  if (titleInput && titleInput.value.trim() !== '') {
    titleEl.textContent = titleInput.value;
    titleEl.style.display = 'block';
    if (titleColor) titleEl.style.color = titleColor.value;
  } else {
    titleEl.style.display = 'none';
  }

  // ===== زیرتیتر =====
  if (subtitleInput && subtitleInput.value.trim() !== '') {
    subEl.textContent = subtitleInput.value;
    subEl.style.display = 'block';
    if (subtitleColor) subEl.style.color = subtitleColor.value;
  } else {
    subEl.style.display = 'none';
  }

  // ===== دکمه =====
  if (btnTextInput) btnEl.textContent = btnTextInput.value || 'ورود';
  if (btnBg) btnEl.style.background = btnBg.value;
  if (btnTextColor) btnEl.style.color = btnTextColor.value;
}

document.addEventListener('input', function (e) {
  if (e.target.matches('.welcome-picker, .field-input, .field-textarea, #welcome_icon, #welcome_title, #welcome_subtitle, #welcome_button_text')) {
    updateWelcomePreview();
  }
});

document.addEventListener('DOMContentLoaded', function () {
  updateWelcomePreview();
});
</script>
