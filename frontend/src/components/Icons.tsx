import type { SVGProps } from 'react';

const base = {
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 1.7,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
};

export const IconMatches = (props: SVGProps<SVGSVGElement>) => (
  <svg {...base} {...props}>
    <circle cx="9" cy="8" r="3.2" />
    <path d="M2.8 19.2c.6-3.1 3.1-4.8 6.2-4.8s5.6 1.7 6.2 4.8" />
    <path d="M16.2 5.6a3 3 0 0 1 0 5.8" />
    <path d="M17.9 14.5c2.1.5 3.4 2 3.8 4.7" />
  </svg>
);

export const IconRequests = (props: SVGProps<SVGSVGElement>) => (
  <svg {...base} {...props}>
    <path d="M4 5.5h16v13H4z" />
    <path d="M4 14h4l1.4 2.2h5.2L16 14h4" />
  </svg>
);

export const IconChats = (props: SVGProps<SVGSVGElement>) => (
  <svg {...base} {...props}>
    <path d="M20.5 12.4c0 3.9-3.8 7-8.5 7a10 10 0 0 1-2.6-.34L4.5 20.5l1.1-3.3A6.6 6.6 0 0 1 3.5 12.4c0-3.9 3.8-7 8.5-7s8.5 3.1 8.5 7Z" />
  </svg>
);

export const IconProfile = (props: SVGProps<SVGSVGElement>) => (
  <svg {...base} {...props}>
    <circle cx="12" cy="8.4" r="3.6" />
    <path d="M4.8 20c.7-3.7 3.6-5.6 7.2-5.6s6.5 1.9 7.2 5.6" />
  </svg>
);

export const IconLogout = (props: SVGProps<SVGSVGElement>) => (
  <svg {...base} {...props}>
    <path d="M14 4.5H6.5v15H14" />
    <path d="M12.5 12h8" />
    <path d="m17.5 8.6 3.4 3.4-3.4 3.4" />
  </svg>
);

export const IconSpark = (props: SVGProps<SVGSVGElement>) => (
  <svg {...base} {...props}>
    <path d="M12 3.5 13.8 9l5.2 1.8-5.2 1.8L12 18l-1.8-5.4L5 10.8 10.2 9 12 3.5Z" />
  </svg>
);

export const IconArrowRight = (props: SVGProps<SVGSVGElement>) => (
  <svg {...base} {...props}>
    <path d="M4.5 12h15" />
    <path d="m14 6.5 5.5 5.5L14 17.5" />
  </svg>
);

export const IconShield = (props: SVGProps<SVGSVGElement>) => (
  <svg {...base} {...props}>
    <path d="M12 3.2 19.4 6v6.1c0 4.2-3 7.1-7.4 8.7-4.4-1.6-7.4-4.5-7.4-8.7V6L12 3.2Z" />
    <path d="m9.2 12.2 2 2 3.6-3.9" />
  </svg>
);

export const IconSun = (props: SVGProps<SVGSVGElement>) => (
  <svg {...base} {...props}>
    <circle cx="12" cy="12" r="4" />
    <path d="M12 2.6v2M12 19.4v2M2.6 12h2M19.4 12h2M5.3 5.3l1.4 1.4M17.3 17.3l1.4 1.4M18.7 5.3l-1.4 1.4M6.7 17.3l-1.4 1.4" />
  </svg>
);

export const IconMoon = (props: SVGProps<SVGSVGElement>) => (
  <svg {...base} {...props}>
    <path d="M20 14.4A8.4 8.4 0 0 1 9.6 4a8.6 8.6 0 1 0 10.4 10.4Z" />
  </svg>
);
