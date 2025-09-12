

const DashBoardCardItems= [
    {
        title: "TODAY'S SPEND",
        amount: 1500,
        currency:"CA$",
        minorText: "vs yesterday:",
        percentageChange: "5",
        trend:"up"
    },
    {
        title: "THIS MONTH",
        amount: 30000,
        currency:"CA$",
        minorText: "vs last month:",
        percentageChange: "10",
        trend:"up"
    },
    {
        title:"TOP SPENDING CATEGORY",
        category: "Food",
        amount: 50,
        currency:"CA$",
        minorText: "of today's spend",
        percemtageChange: "20",
    },
    {
        title: "Total Transactions",
        amount: 6,
        minorText: "Personal only"
    }


]
const DashBoardCards = () => {
  return (
    <div className="mx-auto my-0 w-full max-w-5xl px-4">
    <div className="grid grid-cols-2  lg:grid-cols-4 justify-evenly  w-full gap-6">
        {
            DashBoardCardItems.map((item, index) => (
                <div key={index} className="flex flex-col   rounded-lg shadow-md  bg-[#17171c] w-fit p-4 border-md border-2 border-zinc-500">
                    <h3 className="text-sm text-gray-400">{item.title}</h3>
                    <div className="text-2xl font-bold my-1">
                        {item.category?item.category+" : ":""}{item.currency}{item.amount}
                    </div>
                    <div className="text-sm text-gray-400">
                        {item.minorText} <span className={`text-${item.trend === "up" ? "green" : "red"}-500`}></span>
                            {item.percentageChange? item.percentageChange+"%":""}
                    </div>
                </div>
            ))
        }
    </div>
    </div>
  )
}

export default DashBoardCards