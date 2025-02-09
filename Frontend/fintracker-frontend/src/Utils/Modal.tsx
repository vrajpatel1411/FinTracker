import { Dispatch } from "react"

import { delay, easeInOut, easeOut, motion } from "motion/react"

const Modal = ({error,setModal}:{error:string | null,setModal:Dispatch<boolean>}) => {
  return (
    <motion.div initial={{opacity:50,scale:0.50}} animate={{opacity:100, scale:1}} transition={{ease:'anticipate' ,duration:1}} className="absolute top-0 right-0 w-fit h-fit bg-[#F2994A] m-4 border-0 p-4 rounded-md text-center">
    {/* Close Button */}
    <div 
        className="absolute top-2 right-2 w-6 h-6 flex items-center justify-center cursor-pointer  rounded-full hover:bg-gray-100/25  transition"
        onClick={() => setModal(false)}
    >
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 50 50">
            <path fill="black" d="M7.71875 6.28125 L6.28125 7.71875 L23.5625 25 L6.28125 42.28125 L7.71875 43.71875 L25 26.4375 L42.28125 43.71875 L43.71875 42.28125 L26.4375 25 L43.71875 7.71875 L42.28125 6.28125 L25 23.5625 Z"></path>
        </svg>
    </div>

    {/* Error Message */}
    <div className="text-white m-4">{error}</div>
</motion.div>

  )
}

export default Modal