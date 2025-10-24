import React from 'react'
import PersonIcon from '@mui/icons-material/Person';
import GroupsIcon from '@mui/icons-material/Groups';
import SettingsIcon from '@mui/icons-material/Settings';
interface MenuItem {
  name: string;
  icon: React.ReactNode;
  Link: string;
}

const menuItems:MenuItem[]  = [
{'name':'Personal','icon':<PersonIcon />,Link:'/personal'},
{'name':'Group','icon':<GroupsIcon />,Link:'/Groups'},
{'name':'Settings','icon':<SettingsIcon />,Link:'/settings'},
]
const SideBar = () => {
  return (
    <div className='m-5 h-screen'>
      {
        menuItems.map((item, index) => (
          <div key={index} className="flex items-center justify-start text-lg my-1 px-6 py-2 text-zinc-100 hover:bg-zinc-800 hover:opacity-90  hover:rounded-lg cursor-pointer ">
            <div className="mr-4 text-[#563787]">
              {item.icon}
            </div>
            <div className=" font-bold tracking-wider  ">
              {item.name}
            </div>
          </div>
        ))
      }
    </div>
  )
}

export default SideBar