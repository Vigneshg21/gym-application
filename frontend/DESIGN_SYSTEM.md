# 🎨 Premium Gym Application - Design Reference Guide

## Color System

### Primary Gym Colors
```
Gym Orange:       #ff6b35  ████████
Gym Accent:       #f7931e  ████████
Gym Dark:         #1a2838  ████████
```

### Secondary Colors
```
Ocean Blue:       #117c8f  ████████
Mint Green:       #4f8a5b  ████████
Ember Red:        #b24433  ████████
Sun Orange:       #d46f2d  ████████
```

### Surface Colors
```
Light Surface:    rgba(255, 250, 242, 0.95)
Strong Surface:   rgba(255, 255, 255, 0.98)
Dark Surface:     #112432
Text Primary:     #14212a
Text Muted:       #5f6b72
```

## Typography

### Font Family
- **Display/Headers**: Space Grotesk (700 weight)
- **Body/Content**: Manrope (400-600 weight)
- **Fallback**: Segoe UI, sans-serif

### Sizing Scale
- **H1 (Brand)**: 2.5rem - 3rem
- **H2 (Sections)**: 1.8rem - 2rem
- **H3 (Cards)**: 1.4rem - 1.6rem
- **Body**: 0.95rem - 1rem
- **Small**: 0.78rem - 0.85rem
- **Label**: 0.74rem (uppercase)

## Spacing System

### Standardized Spacing
```
xs:  0.25rem  (4px)
sm:  0.5rem   (8px)
md:  0.75rem  (12px)
base: 1rem    (16px)
lg:  1.5rem   (24px)
xl:  2rem     (32px)
```

### Component Padding
- **Cards**: 1.25rem - 1.5rem
- **Panels**: 1.5rem
- **Buttons**: 0.85rem vertical, 1.4rem horizontal
- **Sidebar**: 2rem (all sides)
- **Main Stage**: 2rem (all sides)

## Border Radius

### Scaling
- **Buttons**: 999px (pill shape)
- **Cards/Panels**: 1.6rem
- **Icons**: 1.2rem
- **Form Inputs**: 1rem
- **Small Elements**: 0.75rem

## Shadow System

### Shadow Levels
```
sm: 0 4px 12px rgba(0, 0, 0, 0.08)
md: 0 8px 20px rgba(0, 0, 0, 0.1)
lg: 0 16px 40px rgba(0, 0, 0, 0.12)
xl: 0 24px 80px rgba(33, 42, 48, 0.16)
premium: 0 30px 100px rgba(26, 40, 56, 0.2)
```

### Shadow Applications
- **Cards at rest**: md shadow
- **Cards on hover**: lg shadow
- **Buttons**: md shadow
- **Elevated elements**: xl shadow
- **Hero sections**: premium shadow

## Component States

### Buttons
- **Primary**: Gradient bg + lg shadow, hover: translateY(-2px) + xl shadow
- **Ghost**: Light bg + border, hover: darker bg

### Form Inputs
- **Default**: Light border
- **Focus**: Gym orange border + glow (3px rgba)
- **Error**: Ember red accent
- **Disabled**: Reduced opacity

### Status Pills
- **Positive**: Mint green gradient
- **Warning**: Sun orange gradient
- **Critical**: Gym orange/ember red gradient
- **Neutral**: Ocean blue gradient

### Cards
- **Default**: Subtle shadow, animation: slideInUp
- **Hover**: Deeper shadow, slight lift (translateY -4px)
- **Active**: Gym orange border highlight

## Animations

### Timing
- **Quick**: 180ms (hover feedback)
- **Standard**: 240ms (transitions)
- **Smooth**: 500ms (entrance)
- **Slow**: 900ms (loading spinner)

### Easing Functions
- **Standard**: ease (default)
- **Smooth**: cubic-bezier(0.34, 1.56, 0.64, 1)
- **Linear**: linear (rotations)

### Available Animations
```css
slideInUp:  Fade in + move up (500ms)
fadeIn:     Pure fade in (300ms)
spin:       360° rotation (900ms infinite)
```

## Layout Breakpoints

### Responsive Grid
- **Desktop**: 280px sidebar + 1fr main
- **Tablet (1180px)**: Single column, nav becomes 3-column grid
- **Mobile (840px)**: Full stack, cards full width
- **Small (640px)**: Table becomes block layout

### Grid Columns
- **Metrics**: 4 columns (desktop), 2 (tablet), 1 (mobile)
- **Two-column**: 2 columns (desktop), 1 (mobile)
- **Plan grid**: auto-fit minmax(220px)
- **Finance cards**: 2 columns (desktop), 1 (mobile)

## Visual Effects

### Hover Effects
- **Lift**: translateY(-2px to -4px)
- **Scale**: scale(1.02) on subtle elements
- **Glow**: Box-shadow expansion
- **Color shift**: Gradient intensification

### Focus Effects
- **Border glow**: Colored border + soft shadow
- **Outline**: 3px colored glow ring
- **Background**: Subtle background change

### Loading States
- **Spinner**: 3rem circle, gym orange border-top
- **Animation**: Continuous 360° rotation
- **Duration**: 0.9s per rotation

## Gradient Patterns

### Hero Section
```
linear-gradient(135deg, 
  rgba(255, 250, 242, 0.95) 0%, 
  rgba(247, 147, 30, 0.08) 100%)
```

### Sidebar Background
```
linear-gradient(180deg, 
  #0f1f2b 0%, 
  #1a2d3a 50%, 
  #0d1820 100%)
```

### Button Primary
```
linear-gradient(135deg, 
  #ff6b35 0%, 
  #d46f2d 100%)
```

### Status Pills
```
linear-gradient(135deg, 
  [color1 @ 0.2-0.25 opacity], 
  [color2 @ 0.1-0.12 opacity])
```

## Accessibility

### Color Contrast
- Text on surfaces: 4.5:1 minimum (WCAG AA)
- UI components: 3:1 minimum (WCAG AA)
- Large text: 3:1 minimum

### Focus States
- All interactive elements: visible focus ring
- Focus indicator: 2px gym orange outline
- Minimum 44px touch target

### Animation Preferences
- Respects prefers-reduced-motion
- No auto-playing animations
- All animations can be disabled

## Browser Compatibility

### Supported Browsers
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

### CSS Features Used
- CSS Grid (layouts)
- CSS Flexbox (alignment)
- CSS Gradients (colors)
- CSS Transforms (animations)
- CSS Backdrop Filter (glass effect)
- CSS Custom Properties (variables)

## Performance Considerations

### Optimizations
- SVG images (scalable, small file size)
- Hardware-accelerated transforms
- Efficient animations (GPU-optimized)
- Minimal repaints/reflows
- Optimized backdrop filters

### Load Times
- Hero image: ~5KB (SVG)
- All icon images: ~10KB combined
- CSS enhancements: Minimal impact
- Animation performance: 60fps

---

## Implementation Checklist

- ✅ Gym theme colors applied throughout
- ✅ Premium shadows and depths added
- ✅ Smooth transitions and animations
- ✅ Enhanced hover states
- ✅ Hero image with gym equipment
- ✅ Status badges with gradients
- ✅ Form input focus states
- ✅ Button styling updates
- ✅ Navigation enhancements
- ✅ Table styling improvements
- ✅ Loading animations
- ✅ Responsive design maintained
- ✅ Accessibility standards met
- ✅ Cross-browser compatibility

---

Use this guide as a reference when maintaining or extending the premium gym application design.
