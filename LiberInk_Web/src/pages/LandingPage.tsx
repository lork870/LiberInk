import { Link } from 'react-router-dom';
import myLanding_img from '../assets/landing_img_1.jpg';

const LandingPage = () => {
  return (
    <div className="min-h-[100dvh] w-full flex flex-col bg-[#F6F3E9] font-serif text-[#4A0404] overflow-hidden">
      
      {/* Навігація - Фіксуємо висоту h-[72px] для точної математики */}
      <nav className="fixed top-0 left-0 w-full h-[72px] flex justify-between items-center px-6 md:px-12 border-b border-[#4A0404]/10 z-50 bg-[#F6F3E9]">
        <div className="flex items-center gap-2 md:gap-3 cursor-pointer">
          <img src="/ic_logo.png" alt="Logo" className="h-8 w-auto"/>
          <span className="text-xl md:text-2xl font-bold">LiberInk</span>
        </div>
        <div className="flex items-center gap-4 md:gap-8 text-sm md:text-base font-sans font-normal">
          <Link to="/" className="hover:opacity-70 transition-opacity hidden md:block">Features</Link>
          <Link to="/login" className="hover:opacity-70 transition-opacity">Login</Link>
          <Link to="/register" className="border border-[#4A0404]/50 px-4 md:px-5 py-1.5 rounded-full hover:bg-[#4A0404] hover:text-[#F6F3E9] transition-all">
            Sign Up
          </Link>
        </div>
      </nav>

      {/* Hero Section */}
      {/* main починається рівно під навігацією (mt-[72px]) і займає рівно залишок екрана (100dvh - 72px) */}
      <main className="min-h-[calc(100dvh-72px)] mt-[72px] flex flex-col items-center justify-center px-6 md:px-20 py-8 w-full lg:flex-row lg:justify-between lg:items-center">
        
        {/* Блок з текстом */}
        {/* Прибрано flex-grow, щоб контент був зібраний в один блок і чітко центрувався */}
        <div className="w-full lg:w-1/2 flex flex-col items-center lg:items-start text-center lg:text-left">
          <h1 className="text-5xl md:text-7xl font-normal leading-[1.1] mb-6 md:mb-8 text-[#4A0404]">
            Your Story,<br /> Your Sanctuary
          </h1>
          
          {/* Зображення на мобільному */}
          <div className="relative w-full max-w-[624px] mb-6 lg:hidden">
            <div className="w-full aspect-[16/10] rounded-[32px] overflow-hidden shadow-[0_10px_30px_-5px_rgba(0,0,0,0.6)] border border-white/10">
              <img src={myLanding_img} alt="Writing" className="w-full h-full object-cover" />
            </div>
          </div>

          <p className="text-base md:text-lg leading-relaxed text-[#1A1A1A]/70 mb-8 md:mb-10 font-sans font-normal max-w-xl">
            LiberInk is a distraction-free writing environment that helps you focus on what matters—your words. Craft, revise, and perfect your stories with powerful version control.
          </p>
          
          <Link 
            to="/onboarding" 
            className="font-[Manrope] bg-[#4A0404] text-[#F6F3E9] px-8 py-4 lg:py-3 rounded-full text-xl lg:text-lg font-normal block w-full text-center lg:inline-block lg:w-auto tracking-wide shadow-[0_10px_30px_-5px_rgba(74,4,4,0.6)] hover:shadow-[0_15px_35px_-5px_rgba(74,4,4,0.5)] hover:-translate-y-0.5 transition-all duration-300"
          >
            Start Your Story Now
          </Link>
        </div>

        {/* Зображення на десктопі */}
        <div className="relative w-full max-w-[624px] hidden lg:block">
            <div className="w-full aspect-[16/10] md:aspect-[3/2] rounded-[32px] md:rounded-[40px] overflow-hidden border border-white/10 shadow-[0_10px_30px_-5px_rgba(0,0,0,0.6)]">
              <img 
                src={myLanding_img} 
                alt="Writing" 
                className="w-full h-full object-cover max-w-full" 
              />
            </div>
          <div className="absolute -z-10 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[120%] h-[120%] bg-orange-200/10 blur-[100px] rounded-full pointer-events-none"></div>
        </div>
      </main>
    </div>
  );
};

export default LandingPage;