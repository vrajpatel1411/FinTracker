import { NavLink } from "react-router";
import PersonIcon from '@mui/icons-material/Person';
import GroupsIcon from '@mui/icons-material/Groups';
import SettingsIcon from '@mui/icons-material/Settings';


const items = [
    { to: "/personal", label: "Personal", Icon: PersonIcon, enabled: true },
    { to: "/groups", label: "Groups", Icon: GroupsIcon, enabled: false },
    { to: "/settings", label: "Settings", Icon: SettingsIcon, enabled: false },
];

const BottomNav = () => (
  <nav 
      className="fixed bottom-0 inset-x-0 h-16 lg:hidden bg-surface-subtle  border-t border-zinc-800/60 z-30 flex"
      role="navigation"
      aria-label="Primary"
      >
       {items.map(({ to, label, Icon, enabled }) =>
                enabled ? (
                    <NavLink
                        key={to}
                        to={to}
                        className={({ isActive }) =>
                            [
                                "flex-1 flex flex-col items-center justify-center gap-0.5 text-[11px] font-medium transition-colors duration-150",
                                isActive ? "text-brand" : "text-fg-4 hover:text-fg-1",
                            ].join(" ")
                        }
                    >
                        <Icon fontSize="small" />
                        {label}
                    </NavLink>
                ) : (
                    <span
                        key={to}
                        aria-disabled
                        className="flex-1 flex flex-col items-center justify-center gap-0.5 text-[11px] font-medium text-fg-5 opacity-45 cursor-not-allowed"
                    >
                        <Icon fontSize="small" />
                        {label}
                    </span>
                )
            )}
  </nav>
);

export default BottomNav;
