---
name: Literary Signal
colors:
  surface: '#fbf9f1'
  surface-dim: '#dcdad2'
  surface-bright: '#fbf9f1'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f6f4ec'
  surface-container: '#f0eee6'
  surface-container-high: '#eae8e0'
  surface-container-highest: '#e4e2db'
  on-surface: '#1b1c17'
  on-surface-variant: '#55423d'
  inverse-surface: '#30312c'
  inverse-on-surface: '#f3f1e9'
  outline: '#88726c'
  outline-variant: '#dbc1ba'
  surface-tint: '#9a452b'
  primary: '#974229'
  on-primary: '#ffffff'
  primary-container: '#b65a3e'
  on-primary-container: '#fffbff'
  inverse-primary: '#ffb59f'
  secondary: '#625e59'
  on-secondary: '#ffffff'
  secondary-container: '#e9e1db'
  on-secondary-container: '#68635f'
  tertiary: '#006762'
  on-tertiary: '#ffffff'
  tertiary-container: '#00837c'
  on-tertiary-container: '#f3fffd'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdbd1'
  primary-fixed-dim: '#ffb59f'
  on-primary-fixed: '#3b0a00'
  on-primary-fixed-variant: '#7b2e16'
  secondary-fixed: '#e9e1db'
  secondary-fixed-dim: '#ccc5c0'
  on-secondary-fixed: '#1e1b18'
  on-secondary-fixed-variant: '#4a4642'
  tertiary-fixed: '#8df4eb'
  tertiary-fixed-dim: '#70d7cf'
  on-tertiary-fixed: '#00201e'
  on-tertiary-fixed-variant: '#00504c'
  background: '#fbf9f1'
  on-background: '#1b1c17'
  surface-variant: '#e4e2db'
  accent-rust: '#BD5F43'
  text-ink: '#1C1917'
  text-muted: '#44403C'
  surface-paper: '#F0EEE6'
  pure-white: '#FFFFFF'
typography:
  display-lg:
    fontFamily: Literata
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Literata
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Literata
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-md:
    fontFamily: Literata
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: Hanken Grotesk
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 30px
  body-md:
    fontFamily: Hanken Grotesk
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 26px
  label-md:
    fontFamily: Hanken Grotesk
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.05em
  caption:
    fontFamily: Hanken Grotesk
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 8px
  gutter: 16px
  margin-mobile: 20px
  margin-desktop: 40px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
  section-gap: 64px
---

## Brand & Style
The brand identity is an "Editorial Modernist" aesthetic—fusing the intellectual, high-legibility tradition of newsprint with the clean, functional precision of a modern developer tool. It targets a professional audience of software engineers and technical writers who value clarity, focus, and deep reading.

The design style is **Minimalist with Editorial Accents**. It relies on heavy whitespace, a warm "paper" background to reduce eye strain, and high-quality serif typography for long-form content. Subtle structural borders and a single, vibrant "Rust" accent color provide a contemporary edge without distracting from the primary task: reading and comprehension.

## Colors
The palette is built around a "Warm Paper" foundation to evoke a physical journal. 
- **Primary (Accent Rust):** Used sparingly for interactive elements, callouts, and emphasis. It provides warmth and high visibility against the neutral base.
- **Neutral (Ink & Paper):** A high-contrast pairing of deep charcoal (#1C1917) and soft off-white (#F0EEE6) creates an accessible reading environment.
- **Secondary (Stone):** Used for metadata, captions, and secondary icons to maintain a clear visual hierarchy.
- **Surfaces:** Utilize subtle tonal shifts between `pure-white` for cards/headers and `surface-paper` for the global background and decorative sections.

## Typography
The typography system employs a "Serif for Stories, Sans for Systems" rule.
- **Serif (Literata):** Used for all headlines and quotes to convey authority and comfort during long-form reading. 
- **Sans-Serif (Hanken Grotesk):** Used for the UI shell, labels, navigation, and body text. It is chosen for its modern, geometric clarity that balances the traditional feel of the serif.
- **Scale:** High contrast between display sizes and body text is essential. Mobile sizes are strictly defined for the largest headlines to prevent layout overflow.
- **Technical Content:** Monospaced fonts (14px) should be used for code blocks with a subtle background container to distinguish from narrative text.

## Layout & Spacing
The layout follows a **Fixed Column Grid** with a maximum content width of 1100px.
- **Core Column:** The main article content is constrained to a readable 720px width to maintain optimal line length.
- **Sidebars:** Fixed-width sidebars (256px) are used on desktop for Table of Contents and Author information. 
- **Vertical Rhythm:** A modular scale based on 8px (Base) creates a consistent "stack" rhythm. `section-gap` (64px) is used to clearly separate high-level article sections (e.g., Article vs. Related Posts).
- **Responsive Behavior:** On mobile, sidebars are replaced by a minimal "Rail" (48px) or hidden in a slide-out menu, while margins shrink to 20px.

## Elevation & Depth
The system uses a **Low-Contrast Outlines & Tonal Layers** approach instead of heavy shadows.
- **Borders:** Structural separation is achieved via `1px` solid borders using `text-ink/5` (very faint charcoal). This creates a sophisticated, architectural feel.
- **Surfaces:** Depth is communicated through color shifts. The main reading surface is `surface` (warm), while secondary cards and the footer use `surface-container` (slightly darker/cooler) to recede.
- **Shadows:** A single `shadow-sm` (low-blur, low-opacity) is used exclusively for floating cards to provide just enough lift to signify interactivity.
- **Backdrop:** A `backdrop-blur-sm` is used on mobile sticky elements (like the navigation rail) to maintain context while keeping the UI layered.

## Shapes
The shape language is primarily **Soft Geometric**. 
- **Default:** A small `2px` radius is used for buttons and structural containers to keep the look precise and professional.
- **Large Elements:** Cards and featured images use `rounded-xl` (8px) on desktop to feel approachable.
- **Interactive Pills:** Tags (categories) and specific action buttons use a `full` (999px) radius to distinguish them as highly interactive, distinct from structural containers.

## Components
- **Buttons:** Primary actions use a solid `accent-rust` background with white text. Secondary actions use a thin border and rust text. Micro-interactions include a `95%` scale-down on press.
- **Cards:** Related post cards feature a `pure-white` surface, a thin `ink/5` border, and a subtle transition where the title shifts to `accent-rust` on hover.
- **Chips/Tags:** Small, pill-shaped elements with a `10%` opacity rust background and bold rust text.
- **Input Fields:** Textareas and inputs use `pure-white` backgrounds with a thin border. Focus states are indicated by a `2px` rust glow at `20%` opacity.
- **Code Blocks:** Encased in a `surface-container` with 8px padding, utilizing a monospaced font at 14px for optimal technical legibility.
- **Navigation:** Top app bar is fixed with a `pure-white` background and a single bottom border. Bottom navigation on mobile uses a semi-transparent blur.