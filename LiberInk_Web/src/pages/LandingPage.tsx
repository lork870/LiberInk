import { Link } from 'react-router-dom';
import myLanding_img from '../assets/landing_img_1.jpg';

const LandingPage = () => {
  return (
    <div className="min-h-screen w-full flex flex-col bg-[#F6F3E9] font-serif text-[#4A0404] overflow-x-hidden">
      
      <nav className="fixed top-0 left-0 w-full flex justify-between items-center px-6 md:px-12 py-4 border-b border-[#4A0404]/10 z-50 bg-[#F6F3E9]">
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
      <main className="flex-grow flex flex-col lg:flex-row items-center justify-center px-6 md:px-20 pt-24 pb-10 gap-8 md:gap-12 w-full">
        <div className="max-w-xl text-center lg:text-left">
          <h1 className="text-5xl md:text-7xl font-normal leading-[1.1] mb-6 md:mb-8 text-[#4A0404]">
            Your Story,<br /> Your Sanctuary
          </h1>
          <p className="text-base md:text-lg leading-relaxed text-[#1A1A1A]/70 mb-8 md:mb-10 font-sans font-normal">
            LiberInk is a distraction-free writing environment that helps you focus on what matters—your words. Craft, revise, and perfect your stories with powerful version control.
          </p>
          <Link 
            to="/onboarding" 
            className="font-[Manrope] bg-[#4A0404] text-[#F6F3E9] px-8 py-3 rounded-full text-lg font-normal hover:bg-[#320a0a] transition-all shadow-md inline-block tracking-wide"
          >
            Start Your Story Now
          </Link>
        </div>

        <div className="relative w-full max-w-[624px]">
          <div className="w-full aspect-[16/10] md:aspect-[3/2] rounded-[32px] md:rounded-[40px] overflow-hidden shadow-xl border border-white/10">
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