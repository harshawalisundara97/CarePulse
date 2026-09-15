# Git & Release Workflow — Follow Strictly

## 1. Branching
- Never commit or push directly to `main`.
- All work (features, bug fixes) happens on a dedicated branch off `main`:
  - `feature/<short-name>` for new features
  - `fix/<short-name>` for bug fixes
- Branch names should be short, lowercase, hyphenated.

## 2. During development
- Keep commits scoped to the branch's purpose.
- Test manually as we go, but do not consider the branch "done" until Section 3 is complete.

## 3. Before opening a Pull Request
Before I say "create a PR" / "let's PR this", you must:
1. Check whether test cases exist for the feature/fix being touched, **and** for any existing related features that currently lack tests. If missing, write them.
2. Run the full test suite locally and show me the results.
3. If GitHub Actions CI is configured, confirm the workflow/build passes (check `.github/workflows/`, and if possible, check the latest run status) before proceeding.
4. Only after tests + build are green, open the PR.
5. Do not silently skip any of steps 1–4. If something can't be run (e.g., no CI configured yet), tell me explicitly instead of assuming it's fine.

## 4. After PR approval & merge
- Once a PR is approved and merged into `main`, **ask me for confirmation before deleting the feature branch** (both local and remote, if applicable). Never delete it automatically.
- Wait for my explicit "yes, delete it" before running the delete.

## 5. General rule of thumb
- Test coverage first, PR second, merge third, branch cleanup last (with my confirmation).
- If any step is ambiguous or CI/test setup is missing in a given repo, flag it and ask rather than guessing.

# Frontend Design Language — Follow for All UI Work

This is the required visual/design system for CarePulse's Android UI. Full spec:
`docs/superpowers/specs/2026-09-07-glass-on-grid-redesign-design.md`.

**Style:** Glass-on-grid. Translucent blurred surfaces (via Haze, `dev.chrisbanes.haze`) over a
ruled 56dp grid and off-screen accent blocks, on Modernist typographic bones (Archivo font,
flush-left uppercase labels, 2px rules, one green accent colour). Glass must always be gated behind
`LocalGlassEnabled` (`ui/theme/Glass.kt`) with an opaque `Background`-tinted fallback at the same
radius/border for low-end devices — layouts must stay legible with zero blur.

**Palette:** `AccentPrimary` green (`#15803D` light / `#4ADE80` dark) for primary actions, active
nav, and the accent field block. Near-black/near-white text (`TextPrimary`), 55%-alpha muted text
(`TextMuted`). `Background` `#F3F2F2` light / `#141312` dark. Full light + dark themes required.
Status pills use a 16%-alpha fill of their own hue with the full-strength hue as text, and always
carry a text label — never color-only.

**Spacing:** 4dp base grid. Screen horizontal padding 20dp. Card padding 16dp standard. Vertical
gap between cards 12dp. Sheet padding 20dp sides / 26dp bottom.

**Radii:** `chip=999dp` · `input=16dp` · `button=18dp` · `statTile=20dp` · `card=22dp` ·
`cardLarge=24dp` · `navBar=26dp` · `sheet=30dp` (top corners only).

**Typography:** Archivo (400/600/700/800) only — do not substitute Roboto or Inter for
glass-on-grid screens. Scale: Display 46sp/800, H1 31sp/800, H2 27sp/800, H3 23sp/800, H4
16sp/800, Body 13.5sp, Label 10–11sp/800 UPPERCASE.

**Navigation:** floating glass bottom nav, 66dp tall, radius 26dp, 3dp accent indicator sliding
along the top edge between 5 equal slots. Keep CarePulse's own tabs per role (Home/Pulse/
Messages/Activity/Settings for Family & Caregiver; Dashboard/Caregivers/Requests/Billing/Settings
for Agency) — do not rename routes.

**Motion:** named durations/easings in `ui/theme/Motion.kt` — screen enter, list stagger, shared-
element profile hero, bottom-sheet slide-in, pulse rings, tab indicator slide, press feedback,
pull-to-refresh spin, sparkline draw. Honour reduce-motion: drop infinite pulse/stagger, keep
fades.

**Accessibility:** minimum touch target 48x48dp, WCAG AA contrast, full-opacity ink on accent/
glass grounds (never alpha-muted text over blur), every icon button has a `contentDescription`.

**Avoid:** hardcoded Roboto/Inter on redesigned screens, color-only status indicators, alpha-
muted text over blurred surfaces, un-gated blur with no fallback path.
