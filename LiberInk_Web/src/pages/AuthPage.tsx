import React, { useState, useEffect } from 'react';
import { auth, googleProvider } from '../firebase';
import { 
  createUserWithEmailAndPassword, 
  signInWithEmailAndPassword, 
  signInWithPopup, 
  updateProfile 
} from 'firebase/auth';
import { Link, useLocation, useNavigate } from 'react-router-dom';

import { GoogleIcon } from '../components/GoogleIcon'; 
import my_ic_Logo from '../assets/ic_logo.png';
import bgTexture from '../assets/paper_texture.jpg'; 

const AuthPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  
  const isLogin = location.pathname === '/login';

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rePassword, setRePassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    setError('');
  }, [isLogin]);

  const checkAndSavePendingBook = async (userId: string) => {
    const pendingBook = localStorage.getItem('onboarding_book');
    if (pendingBook) {
      try {
        const bookData = JSON.parse(pendingBook);
        const response = await fetch('http://localhost:5241/api/Books', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            title: bookData.title,
            description: bookData.pseudonymDescription || "",
            userId: userId,
            authorPseudonym: bookData.penName,
            lastEdited: new Date().toISOString()
          })
        });

        if (response.ok) {
          localStorage.removeItem('onboarding_book');
        }
      } catch (err) {
        console.error("Error saving to MySQL:", err);
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      if (isLogin) {
        await signInWithEmailAndPassword(auth, email, password);
        navigate('/library');
      } else {
        if (password !== rePassword) {
          setError("Passwords do not match");
          setIsLoading(false);
          return;
        }
        const userCredential = await createUserWithEmailAndPassword(auth, email, password);
        await updateProfile(userCredential.user, { displayName: name });
        await checkAndSavePendingBook(userCredential.user.uid);
        navigate('/library');
      }
    } catch (err: any) {
      setError(isLogin ? "Invalid email or password" : err.message.replace("Firebase: ", ""));
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleSignIn = async () => {
    try {
      const result = await signInWithPopup(auth, googleProvider);
      if (!isLogin) await checkAndSavePendingBook(result.user.uid);
      navigate('/library'); 
    } catch (err: any) {
      setError("Failed to authenticate with Google");
    }
  };

  return (
    <div className="min-h-screen bg-[#F6F3E9] flex flex-col items-center justify-start p-2 font-serif text-[#4A0404] overflow-hidden relative">
      
      {/* Header Section */}
      <div className="w-full max-w-[380px] h-24 flex flex-col items-center justify-between mt-12 mb-2 animate-in fade-in duration-700">
        <div className="flex items-center gap-2">
          <img src={my_ic_Logo} alt="Logo" className="h-8 w-auto" />
          <span className="text-2xl font-bold">LiberInk</span>
        </div>
        
        <div className="flex-1 flex flex-col items-center justify-end pb-2 text-center">
          <h1 className="text-2xl font-medium leading-tight">
            {isLogin ? "Welcome to service" : "Create Your Account"}
          </h1>
          <p className="text-[#4A0404]/60 font-sans text-sm mt-1">
            {isLogin ? "Sign in to continue your story" : "Start writing your story today"}
          </p>
        </div>
      </div>

      {/* Main Card Container */}
      <div className="w-full max-w-[380px] flex flex-col items-center">
        <div 
          className={`w-full rounded-[32px] shadow-[0_15px_40px_rgba(74,4,4,0.12)] px-8 py-8 border border-[#4A0404]/5 overflow-hidden relative transition-all duration-500 ease-in-out ${isLogin ? 'max-h-[500px]' : 'max-h-[750px]'}`}
        >
          
          <div 
            className="absolute top-0 left-0 w-full h-[800px] pointer-events-none"
            style={{ backgroundImage: `url(${bgTexture})`, backgroundSize: 'cover', backgroundPosition: 'top center' }}
          ></div>

          <div className="absolute inset-0 bg-white/70 pointer-events-none"></div>

          <div className="relative z-10 text-center">
            <form onSubmit={handleSubmit} className="flex flex-col text-left font-sans">
              
              <div 
                className={`transition-all duration-500 ease-in-out overflow-hidden ${isLogin ? 'max-h-0 opacity-0 mb-0 pt-0' : 'max-h-[100px] opacity-100 mb-4 pt-3'}`}
              >
                <div className="relative">
                  <label 
                    className="absolute top-0 left-4 -translate-y-1/2 z-20 px-1.5 text-[12px] font-medium text-[#4A0404]/80 bg-white/70 rounded-sm"
                    style={{ backgroundImage: `url(${bgTexture})`, backgroundSize: '800%', backgroundPosition: 'center' }}
                  >
                    <div className="absolute inset-0 bg-white/70 -z-10"></div>
                    Name
                  </label>
                  <input 
                    type="text" 
                    placeholder="Your Name" 
                    className="w-full pl-6 pr-5 py-3 rounded-xl border border-[#4A0404]/30 bg-transparent outline-none focus:border-[#4A0404] transition-all text-sm placeholder:text-[#4A0404]/20"
                    value={name} 
                    onChange={(e) => setName(e.target.value)} 
                    required={!isLogin} 
                  />
                </div>
              </div>

              {/* Поле: Email */}
              <div className="relative mb-4 mt-2">
                <label className="absolute top-0 left-4 -translate-y-1/2 z-20 px-1.5 text-[12px] font-medium text-[#4A0404]/80 bg-white/70 rounded-sm"
                  style={{ backgroundImage: `url(${bgTexture})`, backgroundSize: '800%', backgroundPosition: 'center' }}>
                  <div className="absolute inset-0 bg-white/70 -z-10"></div>
                  Email
                </label>
                <input 
                  type="email" 
                  placeholder="You@example.com" 
                  className="w-full pl-6 pr-5 py-3 rounded-xl border border-[#4A0404]/30 bg-transparent outline-none focus:border-[#4A0404] transition-all text-sm placeholder:text-[#4A0404]/20"
                  value={email} 
                  onChange={(e) => setEmail(e.target.value)} 
                  required 
                />
              </div>

              {/* Поле: Password */}
              <div className="relative mb-4 mt-2">
                <label className="absolute top-0 left-4 -translate-y-1/2 z-20 px-1.5 text-[12px] font-medium text-[#4A0404]/80 bg-white/70 rounded-sm"
                  style={{ backgroundImage: `url(${bgTexture})`, backgroundSize: '800%', backgroundPosition: 'center' }}>
                  <div className="absolute inset-0 bg-white/70 -z-10"></div>
                  Password
                </label>
                <input
                  type="password"
                  placeholder="••••••••"
                  className="w-full pl-6 pr-5 py-3 rounded-xl border border-[#4A0404]/30 bg-transparent outline-none focus:border-[#4A0404] transition-all text-sm 
                             placeholder:text-lg placeholder:tracking-[2px] placeholder:text-[#4A0404]/25 placeholder:translate-y-[1px]"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>

              {/* Поле: RePassword */}
              <div 
                className={`transition-all duration-500 ease-in-out overflow-hidden ${isLogin ? 'max-h-0 opacity-0 mb-0 pt-0' : 'max-h-[100px] opacity-100 mb-4 pt-3'}`}
              >
                <div className="relative">
                  <label className="absolute top-0 left-4 -translate-y-1/2 z-20 px-1.5 text-[12px] font-medium text-[#4A0404]/80 bg-white/70 rounded-sm"
                    style={{ backgroundImage: `url(${bgTexture})`, backgroundSize: '800%', backgroundPosition: 'center' }}>
                    <div className="absolute inset-0 bg-white/70 -z-10"></div>
                    RePassword
                  </label>
                  <input
                    type="password"
                    placeholder="••••••••"
                    className="w-full pl-6 pr-5 py-3 rounded-xl border border-[#4A0404]/30 bg-transparent outline-none focus:border-[#4A0404] transition-all text-sm 
                               placeholder:text-lg placeholder:tracking-[2px] placeholder:text-[#4A0404]/25 placeholder:translate-y-[1px]"
                    value={rePassword}
                    onChange={(e) => setRePassword(e.target.value)}
                    required={!isLogin}
                  />
                </div>
              </div>

              {error && <p className="text-red-600 text-[10px] text-center font-sans mb-4">{error}</p>}

              <button
                type="submit"
                disabled={isLoading}
                className="w-full py-3 bg-[#4A0404] text-[#F6F3E9] font-medium rounded-full hover:bg-[#320a0a] active:scale-95 transition-all text-base shadow-md shadow-[#4A0404]/20"
              >
                {isLoading ? (isLogin ? "Signing in..." : "Creating...") : (isLogin ? "Sign in" : "Create Account")}
              </button>
            </form>

            <div className="my-4 flex items-center before:flex-1 before:border-t before:border-[#4A0404]/10 after:flex-1 after:border-t after:border-[#4A0404]/10">
              <span className="px-2 text-[#4A0404]/40 text-[10px] font-sans uppercase tracking-widest font-bold">Or continue with</span>
            </div>

            <button 
              onClick={handleGoogleSignIn}
              type="button"
              className="w-full py-2.5 px-4 border border-[#4A0404]/20 rounded-full flex items-center justify-center gap-2 hover:bg-white/40 transition-all active:scale-[0.98] shadow-sm"
            >
              <GoogleIcon />
              <span className="font-semibold text-[#4A0404]/80 text-sm font-sans">Sign in with Google</span>
            </button>

            <p className="mt-8 text-sm text-[#4A0404]/60 font-sans">
              {isLogin ? "Don't have an account? " : "Already have an account? "}
              <Link 
                to={isLogin ? "/register" : "/login"} 
                className="text-[#4A0404] font-bold hover:underline"
              >
                {isLogin ? "Sign up" : "Sign in"}
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AuthPage;