import PersonIcon from "@mui/icons-material/Person";
import GroupsIcon from "@mui/icons-material/Groups";
import SettingsIcon from "@mui/icons-material/Settings";
import { NavLink } from "react-router";

const items = [
    { to: "/personal", label: "Personal", Icon: PersonIcon, enabled: true },
    { to: "/groups", label: "Groups", Icon: GroupsIcon, enabled: false },
    { to: "/settings", label: "Settings", Icon: SettingsIcon, enabled: false },
];
const SideBar = ()=> {
    return (
        <aside className="hidden lg:flex w-58 flex-col gap-1 p-4 bg-surface-subtle border-r border-zinc-800/50 min-h-screen">
            {items.map(({ to, label, Icon, enabled }) =>
                enabled ? (
                    <NavLink
                        key={to}
                        to={to}
                        className={({ isActive }) =>
                            [
                                "flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-colors duration-150 border border-transparent",
                                isActive
                                    ? "bg-brand/10 text-brand border-brand/20"
                                    : "text-fg-3 hover:bg-zinc-800/60 hover:text-fg-1",
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
                        className="flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium text-fg-5 opacity-45 cursor-not-allowed"
                    >
                        <Icon fontSize="small" />
                        {label}
                        <span className="ml-auto text-[10px] text-fg-5 mr-1">Soon</span>
                    </span>
                )
            )}
        </aside>
    );
}

export default SideBar;
