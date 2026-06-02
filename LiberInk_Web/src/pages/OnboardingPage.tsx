import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import my_ic_Logo from '../assets/ic_logo.png';
import bgTexture from '../assets/paper_texture.jpg'; 

const OnboardingPage = () => {
  const [currentStep, setCurrentStep] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const [bookData, setNoteData] = useState({
    title: '',
    description: '',         
    penName: '',             
    pseudonymDescription: '' 
  });

  const handleNext = () => {
    if (bookData.title.trim()) setCurrentStep(2);
  };

  const handleBack = () => {
    if (currentStep > 1) setCurrentStep(currentStep - 1);
  };

  const handleComplete = async () => {
    if (!bookData.penName.trim()) return;
    setIsLoading(true);
    
    setTimeout(() => {
      localStorage.setItem('onboarding_book', JSON.stringify(bookData));
      setIsLoading(false);
      navigate('/register');
    }, 800);
  };

  return (
    <div className="min-h-screen bg-[#F6F3E9] flex flex-col items-center p-4 md:p-6 font-serif text-[#4A0404]">
      
      <div className="w-full max-w-6xl flex justify-center py-4 md:py-6 mt-4 md:mt-0">
         <div className="flex items-center gap-2 md:gap-3">
            <img src={my_ic_Logo} alt="Logo" className="h-6 md:h-8 w-auto" />
            <span className="text-2xl md:text-4xl font-bold">LiberInk</span>
         </div>
      </div>

      <div className="text-center mb-6 md:mb-8 animate-in fade-in slide-in-from-top-4 duration-700 mt-4 md:mt-0">
        <h1 className="text-2xl md:text-4xl font-medium leading-tight px-4">
          Let's Create Your First Book
        </h1>
      </div>

      <div 
        className="w-full max-w-xl rounded-[24px] md:rounded-[32px] shadow-[0_20px_50px_rgba(74,4,4,0.12)] px-5 md:px-8 py-6 md:py-8 border border-[#4A0404]/5 overflow-hidden relative"
        style={{ 
          backgroundImage: `url(${bgTexture})`, 
          backgroundSize: 'cover',
          backgroundPosition: 'center'
        }}
      >
        <div className="absolute inset-0 bg-white/60 pointer-events-none"></div>

        <div className="relative z-10">
          <div className="flex items-center justify-between mb-6 md:mb-8">
            <div className="flex items-center gap-3">
              <div className="bg-[#4A0404] text-[#F6F3E9] w-8 h-8 rounded-full flex items-center justify-center text-sm md:text-base font-bold font-sans shrink-0">
                {currentStep}
              </div>
              <h2 className="text-lg md:text-xl font-medium truncate pr-2">
                {currentStep === 1 ? "Book Details" : "Pseudonym"}
              </h2>
            </div>

            {currentStep === 2 && (
              <button 
                onClick={handleBack}
                className="text-[12px] md:text-[13px] font-sans font-medium text-[#4A0404]/60 hover:text-[#4A0404] transition-colors whitespace-nowrap"
              >
                ← Previous
              </button>
            )}
          </div>

          <div className="space-y-6 md:space-y-10 mb-8 md:mb-10">
            {currentStep === 1 ? (
              <>
                <div className="relative">
                  <label 
                    className="absolute top-0 left-4 md:left-5 -translate-y-1/2 z-20 px-1.5 text-[12px] md:text-[13px] font-medium text-[#4A0404]/80 font-sans flex items-center bg-white/60 rounded-sm"
                    style={{ 
                        backgroundImage: `url(${bgTexture})`, 
                        backgroundSize: '800%',
                        backgroundPosition: 'center'
                    }}
                  >
                    <div className="absolute inset-0 bg-white/60 -z-10"></div>
                    Book Title *
                  </label>
                  <input 
                    type="text"
                    placeholder="Enter your book title"
                    className="w-full px-4 md:px-5 py-3 md:py-3.5 rounded-xl border border-[#4A0404]/30 bg-transparent outline-none focus:border-[#4A0404] transition-all font-sans text-sm md:text-base placeholder:text-[#4A0404]/20"
                    value={bookData.title}
                    onChange={(e) => setNoteData({...bookData, title: e.target.value})}
                  />
                </div>

                <div className="relative mt-2 md:mt-0">
                  <label 
                    className="absolute top-0 left-4 md:left-5 -translate-y-1/2 z-20 px-1.5 text-[12px] md:text-[13px] font-medium text-[#4A0404]/80 font-sans flex items-center bg-white/60 rounded-sm"
                    style={{ 
                        backgroundImage: `url(${bgTexture})`, 
                        backgroundSize: '800%',
                        backgroundPosition: 'center'
                    }}
                  >
                    <div className="absolute inset-0 bg-white/60 -z-10"></div>
                    Description (Optional)
                  </label>
                  <textarea 
                    rows={4}
                    placeholder="A brief description or annotation"
                    className="w-full px-4 md:px-5 py-3 md:py-3.5 rounded-xl border border-[#4A0404]/30 bg-transparent outline-none focus:border-[#4A0404] transition-all font-sans resize-none text-sm md:text-base placeholder:text-[#4A0404]/20"
                    value={bookData.pseudonymDescription}
                    onChange={(e) => setNoteData({...bookData, pseudonymDescription: e.target.value})}
                  />
                </div>
              </>
            ) : (
              <>
                <div className="relative">
                  <label 
                    className="absolute top-0 left-4 md:left-5 -translate-y-1/2 z-20 px-1.5 text-[12px] md:text-[13px] font-medium text-[#4A0404]/80 font-sans flex items-center bg-white/60 rounded-sm"
                    style={{ 
                        backgroundImage: `url(${bgTexture})`, 
                        backgroundSize: '800%',
                        backgroundPosition: 'center'
                    }}
                  >
                    <div className="absolute inset-0 bg-white/60 -z-10"></div>
                    Pen Name *
                  </label>
                  <input 
                    type="text"
                    placeholder="Enter your pseudonym"
                    className="w-full px-4 md:px-5 py-3 md:py-3.5 rounded-xl border border-[#4A0404]/30 bg-transparent outline-none focus:border-[#4A0404] transition-all font-sans text-sm md:text-base placeholder:text-[#4A0404]/20"
                    value={bookData.penName}
                    onChange={(e) => setNoteData({...bookData, penName: e.target.value})}
                  />
                </div>

                <div className="relative mt-2 md:mt-0">
                  <label 
                    className="absolute top-0 left-4 md:left-5 -translate-y-1/2 z-20 px-1.5 text-[12px] md:text-[13px] font-medium text-[#4A0404]/80 font-sans flex items-center bg-white/60 rounded-sm"
                    style={{ 
                        backgroundImage: `url(${bgTexture})`, 
                        backgroundSize: '800%',
                        backgroundPosition: 'center'
                    }}
                  >
                    <div className="absolute inset-0 bg-white/60 -z-10"></div>
                    Author Bio (Optional)
                  </label>
                  <textarea 
                    rows={4}
                    placeholder="A brief description of your pseudonym"
                    className="w-full px-4 md:px-5 py-3 md:py-3.5 rounded-xl border border-[#4A0404]/30 bg-transparent outline-none focus:border-[#4A0404] transition-all font-sans resize-none text-sm md:text-base placeholder:text-[#4A0404]/20"
                    value={bookData.description}
                    onChange={(e) => setNoteData({...bookData, description: e.target.value})}
                  />
                </div>
              </>
            )}
          </div>

          <button 
            onClick={currentStep === 1 ? handleNext : handleComplete}
            disabled={isLoading || (currentStep === 1 ? !bookData.title : !bookData.penName)}
            className={`w-full py-3.5 md:py-4 rounded-[20px] md:rounded-[24px] font-bold text-sm md:text-base flex items-center justify-center gap-2 transition-all duration-300 font-sans shadow-md
              ${(currentStep === 1 ? !bookData.title : !bookData.penName)
                ? 'bg-[#4A0404]/30 text-[#4A0404]/50 cursor-not-allowed shadow-none' 
                : 'bg-[#4A0404] text-[#F6F3E9] hover:bg-[#320a0a] active:scale-95 shadow-[#4A0404]/20'
              }`}
          >
            {isLoading ? (
               <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
            ) : (
              <>
                <span>{currentStep === 1 ? "Next Step" : "Complete & Register"}</span>
                <span className="text-lg md:text-xl leading-none">→</span>
              </>
            )}
          </button>
        </div>
      </div>

      <button 
        onClick={() => navigate('/library')}
        className="mt-8 mb-4 text-[#4A0404]/60 hover:text-[#4A0404] transition-colors font-sans text-sm md:text-base underline underline-offset-4 decoration-[#4A0404]/10"
      >
        Skip to library
      </button>
    </div>
  );
};

export default OnboardingPage;