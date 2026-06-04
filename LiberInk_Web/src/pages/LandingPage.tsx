import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

import img1 from '../assets/landing_img_1.jpg';
import img2 from '../assets/landing_img_2.jpg'; 
import img3 from '../assets/landing_img_3.jpg';

const SLIDES = [
  {
    title: "Your Story,\nYour Sanctuary",
    description: "LiberInk is a distraction-free writing environment that helps you focus on what matters—your words. Craft, revise, and perfect your stories with powerful version control.",
    image: img1
  },
  {
    title: "Write Boundlessly,\nEverywhere",
    description: "Start writing over morning coffee on your phone and continue in the evening on your laptop. Your work syncs instantly and securely across all your devices.",
    image: img2
  },
  {
    title: "Control\nEvery Idea",
    description: "Don't be afraid to experiment. Our system automatically saves all your changes, allowing you to easily compare drafts and return to previous ideas.",
    image: img3
  }
];

// НОВИЙ КОМПОНЕНТ: Ефект друкарської машинки
const Typewriter = ({ text }: { text: string }) => {
  const [displayedText, setDisplayedText] = useState('');

  useEffect(() => {
    setDisplayedText(''); // Очищаємо текст при зміні слайду
    let currentText = '';
    let currentIndex = 0;

    const intervalId = setInterval(() => {
      currentText += text[currentIndex];
      setDisplayedText(currentText);
      currentIndex++;

      if (currentIndex >= text.length) {
        clearInterval(intervalId); // Зупиняємо, коли все надруковано
      }
    }, 45); // Швидкість друку (45мс на літеру - оптимально для читання)

    return () => clearInterval(intervalId);
  }, [text]);

  return (
    <>
      {displayedText.split('\n').map((line, i, arr) => (
        <React.Fragment key={i}>
          {line}
          {i < arr.length - 1 && <br />}
        </React.Fragment>
      ))}
    </>
  );
};

const LandingPage = () => {
  const [currentSlide, setCurrentSlide] = useState(0);
  const [isFading, setIsFading] = useState(false);
  const [isPaused, setIsPaused] = useState(false); 
  const [touchStart, setTouchStart] = useState(0);
  const [touchEnd, setTouchEnd] = useState(0);

  const changeSlide = (directionOrIndex: 'next' | 'prev' | number) => {
    if (isFading) return; 
    setIsFading(true); 
    
    setTimeout(() => {
      if (directionOrIndex === 'next') {
        setCurrentSlide((prev) => (prev === SLIDES.length - 1 ? 0 : prev + 1));
      } else if (directionOrIndex === 'prev') {
        setCurrentSlide((prev) => (prev === 0 ? SLIDES.length - 1 : prev - 1));
      } else {
        setCurrentSlide(directionOrIndex);
      }
      setIsFading(false); 
    }, 400); 
  };

  useEffect(() => {
    if (isPaused) return; 

    const timer = setInterval(() => {
      changeSlide('next');
    }, 12000); 

    return () => clearInterval(timer);
  }, [isPaused, currentSlide, isFading]); 

  const handleTouchStart = (e: React.TouchEvent) => {
    setTouchStart(e.targetTouches[0].clientX);
    setIsPaused(true); 
  };
  const handleTouchMove = (e: React.TouchEvent) => setTouchEnd(e.targetTouches[0].clientX);
  const handleTouchEnd = () => {
    setIsPaused(false); 
    if (!touchStart || !touchEnd) return;
    const distance = touchStart - touchEnd;
    
    if (distance > 50) changeSlide('next');
    if (distance < -50) changeSlide('prev');
    
    setTouchStart(0);
    setTouchEnd(0);
  };

  const fadeClass = `transition-all duration-500 ease-in-out ${isFading ? 'opacity-0 scale-[0.98]' : 'opacity-100 scale-100'}`;

  return (
    <div className="min-h-screen w-full flex flex-col bg-[#F6F3E9] font-serif text-[#4A0404] overflow-hidden">
      
      {/* Навігація */}
      <nav className="fixed top-0 left-0 w-full flex justify-between items-center px-6 md:px-12 py-4 border-b border-[#4A0404]/10 z-50 bg-[#F6F3E9]">
        <div className="flex items-center gap-2 md:gap-3 cursor-pointer">
          <img src="/ic_logo.png" alt="Logo" className="h-8 w-auto"/>
          <span className="text-xl md:text-2xl font-bold">LiberInk</span>
        </div>
        <div className="flex items-center gap-4 md:gap-8 text-sm md:text-base font-sans font-normal">
          <Link to="/" className="hover:opacity-70 transition-opacity hidden md:block active:scale-95">Features</Link>
          <Link to="/login" className="hover:opacity-70 transition-opacity active:scale-95">Login</Link>
          <Link 
            to="/register" 
            className="border border-[#4A0404]/50 px-4 md:px-5 py-1.5 rounded-full hover:bg-[#4A0404] hover:text-[#F6F3E9] transition-all duration-300 active:scale-95"
          >
            Sign Up
          </Link>
        </div>
      </nav>

      {/* Hero Section */}
      <main 
        className="h-[calc(100vh-80px)] mt-20 relative flex flex-col items-center justify-between px-6 md:px-20 py-6 w-full lg:flex-row lg:justify-between lg:items-center"
        onTouchStart={handleTouchStart}
        onTouchMove={handleTouchMove}
        onTouchEnd={handleTouchEnd}
        onMouseEnter={() => setIsPaused(true)}   
        onMouseLeave={() => setIsPaused(false)}  
      >
        
        <div className="w-full lg:w-1/2 flex flex-col items-center lg:items-start text-center lg:text-left flex-grow justify-center relative">
          
          <h1 className={`text-4xl md:text-7xl font-normal leading-[1.1] mb-6 md:mb-8 text-[#4A0404] ${fadeClass} min-h-[110px] md:min-h-[160px]`}>
            {!isFading && <Typewriter text={SLIDES[currentSlide].title} />}
          </h1>
          
          <div className="relative w-full max-w-[624px] my-6 lg:hidden">
            <div className={`w-full aspect-[16/10] rounded-[32px] overflow-hidden shadow-[0_10px_30px_-5px_rgba(0,0,0,0.6)] border border-white/10 ${fadeClass}`}>
              <img src={SLIDES[currentSlide].image} alt="Writing" className="w-full h-full object-cover" />
            </div>
          </div>

          <p className={`text-base md:text-lg leading-relaxed text-[#1A1A1A]/70 mb-6 md:mb-10 font-sans font-normal max-w-xl ${fadeClass}`}>
            {SLIDES[currentSlide].description}
          </p>
          
          <div className="flex justify-center gap-2 w-full mb-8 lg:hidden z-10">
            {SLIDES.map((_, index) => (
              <button
                key={index}
                onClick={() => changeSlide(index)}
                className={`h-2.5 rounded-full transition-all duration-500 ${
                  currentSlide === index ? 'w-6 bg-[#4A0404]' : 'w-2.5 bg-[#4A0404]/30'
                }`}
              />
            ))}
          </div>

          <Link
            to="/onboarding" 
            className="font-[Manrope] bg-[#4A0404] text-[#F6F3E9] px-8 py-4 lg:py-3 rounded-full text-xl lg:text-lg font-normal block w-full text-center lg:inline-block lg:w-auto tracking-wide shadow-[0_10px_30px_-5px_rgba(74,4,4,0.6)] hover:shadow-[0_15px_35px_-5px_rgba(74,4,4,0.5)] hover:-translate-y-0.5 active:scale-[0.97] active:shadow-none transition-all duration-300 z-10"
          >
            Start Your Story Now
          </Link>
        </div>

        {/* Десктопне зображення */}
        <div className="relative w-full max-w-[624px] hidden lg:block">
            <div className={`w-full aspect-[16/10] md:aspect-[3/2] rounded-[32px] md:rounded-[40px] overflow-hidden border border-white/10 shadow-[0_10px_30px_-5px_rgba(0,0,0,0.6)] ${fadeClass}`}>
              <img 
                src={SLIDES[currentSlide].image} 
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