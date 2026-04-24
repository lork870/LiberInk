import { Link } from 'react-router-dom';
import myLanding_img from '../assets/Landing_img_1.jpg';
import my_ic_Logo from '../assets/ic_logo.png';

const LandingPage = () => {
  return (
    <div className="min-h-screen bg-[#F6F3E9] font-serif text-[#4A0404]">
      {/* Навігаційна панель */}
      <nav className="flex justify-between items-center px-12 py-4 border-b border-[#4A0404]/10 sticky top-0 z-50 bg-[#F6F3E9]">
        <div className="flex items-center gap-3 cursor-pointer">
          <img src={my_ic_Logo} alt="LiberInk Logo" className="h-8 w-auto" /> 
          <span className="text-2xl font-bold">LiberInk</span>
        </div>
        
        <div className="flex items-center gap-8 text-base font-sans font-normal">
          <Link to="/" className="hover:opacity-70 transition-opacity">Features</Link>
          <Link to="/login" className="hover:opacity-70 transition-opacity text-[#4A0404]">Login</Link>
          <Link 
            to="/register" 
            // Бордер Sign Up: колір 4A0404 з прозорістю 50% (/50)
            className="border border-[#4A0404]/50 px-5 py-1.5 rounded-full hover:bg-[#4A0404] hover:text-[#F6F3E9] transition-all font-normal text-[#4A0404]"
          >
            Sign Up
          </Link>
        </div>
      </nav>

      {/* Hero Section */}
      <main className="flex flex-col md:flex-row items-center justify-between px-20 mt-20 gap-12 pb-20">
        <div className="max-w-xl">
          {/* Заголовок: колір 4A0404 */}
          <h1 className="text-7xl font-normal leading-[1.1] mb-8 text-[#4A0404]">
            Your Story,<br />
            Your Sanctuary
          </h1>
          {/* Опис: колір 1A1A1A з прозорістю 70% (/70) */}
          <p className="text-lg leading-relaxed text-[#1A1A1A]/70 mb-10 font-sans font-normal max-w-lg">
            LiberInk is a distraction-free writing environment that helps you 
            focus on what matters—your words. Craft, revise, and perfect 
            your stories with powerful version control.
          </p>
          {/* Головна кнопка: колір 4A0404 */}
          <Link 
            to="/onboarding" 
            className="bg-[#4A0404] text-[#F6F3E9] px-8 py-3 rounded-full text-lg font-normal hover:bg-[#320a0a] transition-all transform active:scale-95 inline-block font-sans shadow-md"
          >
            Start Your Story Now
          </Link>
        </div>

        {/* Зображення */}
        <div className="relative">
          <div className="w-[624px] h-[400px] rounded-[40px] overflow-hidden shadow-xl border border-white/10">
            <img 
              src={myLanding_img}
              alt="Cozy writing spot"
              className="w-full h-full object-cover"
            />
          </div>
          <div className="absolute -z-10 top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[120%] h-[120%] bg-orange-200/10 blur-[100px] rounded-full"></div>
        </div>
      </main>
    </div>
  );
};

export default LandingPage;