import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../firebase';

// Material Icons
import PersonIcon from '@mui/icons-material/Person';
import LanguageIcon from '@mui/icons-material/Language';
import TuneIcon from '@mui/icons-material/Tune'; 
import EditIcon from '@mui/icons-material/Edit';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import KeyboardArrowRightIcon from '@mui/icons-material/KeyboardArrowRight';
import SaveIcon from '@mui/icons-material/Save';
import CloseIcon from '@mui/icons-material/Close';

interface UserData {
  name: string;
  pseudonym: string;
  email: string;
  memberSince: string;
}

const SettingsPage = () => {
  const navigate = useNavigate();
  const [activeSetting, setActiveSetting] = useState('Profile');
  const [userData, setUserData] = useState<UserData | null>(null);
  const [loading, setLoading] = useState(true);
  
  // Стейт для режиму редагування
  const [isEditing, setIsEditing] = useState(false);
  const [editName, setEditName] = useState('');
  const [editPseudonym, setEditPseudonym] = useState('');

  useEffect(() => {
    const unsubscribe = auth.onAuthStateChanged((user) => {
      if (user) {
        fetchUserProfile(user);
      } else {
        navigate('/login');
      }
    });
    
    return () => unsubscribe();
  }, [navigate]);

  const fetchUserProfile = async (user: any) => {
    try {
      const response = await fetch(`https://liber-ink-api.onrender.com/api/Users/${user.uid}`);
      let apiData = null;
      
      if (response.ok) {
        apiData = await response.json();
      }

      const registrationDate = apiData?.registrationDate 
        ? new Date(apiData.registrationDate) 
        : (user.metadata.creationTime ? new Date(user.metadata.creationTime) : new Date());

      const data = {
        name: apiData?.fullName || user.displayName || "Назар Зайцев",
        pseudonym: apiData?.pseudonym || "Lola",
        email: user.email || 'nazarzajcev3@gmail.com',
        memberSince: registrationDate.toLocaleDateString('uk-UA')
      };

      setUserData(data);
      setEditName(data.name);
      setEditPseudonym(data.pseudonym);
      
    } catch (error) {
      console.error("Помилка:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    // Тут буде твій запит до API (PUT/PATCH)
    // Наразі просто оновлюємо локальний стейт
    if (userData) {
      setUserData({
        ...userData,
        name: editName,
        pseudonym: editPseudonym
      });
    }
    setIsEditing(false);
  };

  const handleCancel = () => {
    if (userData) {
      setEditName(userData.name);
      setEditPseudonym(userData.pseudonym);
    }
    setIsEditing(false);
  };

  if (loading) {
    return (
      <div className="flex-1 h-full bg-[#F9F5EB] flex items-center justify-center font-serif text-[#4A0E0E]">
        Завантаження...
      </div>
    );
  }

  return (
    <div className="flex flex-col md:flex-row flex-1 overflow-hidden bg-[#F9F5EB]">
      {/* 1. INTERNAL SETTINGS MENU (Адаптовано: горизонтальне меню на мобільних) */}
      <aside className="w-full md:w-64 bg-white border-b md:border-b-0 md:border-r border-[#E8E2D2] p-4 md:p-6 shrink-0 flex flex-row md:flex-col gap-2 z-10 overflow-x-auto no-scrollbar">
        <h2 className="hidden md:block text-gray-400 text-[10px] font-bold uppercase tracking-widest mb-4 ml-2 text-left">
          Settings
        </h2>
        
        <button 
          onClick={() => setActiveSetting('Profile')} 
          className={`flex items-center gap-2 md:gap-3 px-4 py-2.5 md:py-3 rounded-xl transition-all shrink-0 md:shrink border md:border-none ${activeSetting === 'Profile' ? 'bg-[#F3EAD3] text-[#4A0E0E] shadow-sm border-[#E8E2D2]' : 'text-gray-600 hover:bg-gray-50 border-transparent'}`}
        >
          <PersonIcon fontSize="small" />
          <span className="font-bold text-sm">Profile</span>
        </button>
        
        <button 
          onClick={() => setActiveSetting('Language')} 
          className={`flex items-center gap-2 md:gap-3 px-4 py-2.5 md:py-3 rounded-xl transition-all shrink-0 md:shrink border md:border-none ${activeSetting === 'Language' ? 'bg-[#F3EAD3] text-[#4A0E0E] border-[#E8E2D2]' : 'text-gray-600 hover:bg-gray-50 border-transparent'}`}
        >
          <LanguageIcon fontSize="small" />
          <span className="font-bold text-sm">Language</span>
        </button>
        
        <button 
          onClick={() => setActiveSetting('References')} 
          className={`flex items-center gap-2 md:gap-3 px-4 py-2.5 md:py-3 rounded-xl transition-all shrink-0 md:shrink border md:border-none ${activeSetting === 'References' ? 'bg-[#F3EAD3] text-[#4A0E0E] border-[#E8E2D2]' : 'text-gray-600 hover:bg-gray-50 border-transparent'}`}
        >
          <TuneIcon fontSize="small" />
          <span className="font-bold text-sm">References</span>
        </button>
      </aside>

      {/* 2. CONTENT AREA */}
      <main className="flex-1 p-4 sm:p-8 md:p-12 overflow-y-auto bg-[#F9F5EB] text-left scrollbar-hide">
        <div className="max-w-6xl mx-auto">
          
          <h1 className="text-3xl md:text-4xl font-serif text-[#433D33] mb-1 md:mb-2 font-medium">
            {isEditing ? 'Edit Profile' : 'Account'}
          </h1>
          <p className="text-gray-500 mb-6 md:mb-10 text-xs md:text-sm">
            {isEditing ? 'Update your personal information below' : 'Manage your account settings and view your statistics'}
          </p>

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8 mb-12">
            
            {/* Left Column */}
            <div className="lg:col-span-7 flex flex-col gap-6 md:gap-8">
              <div className="bg-white p-6 md:p-8 rounded-[24px] md:rounded-[32px] shadow-sm flex flex-col sm:flex-row gap-6 md:gap-8 border border-[#E8E2D2]/30 transition-all items-center sm:items-start">
                
                <div className="flex flex-col items-center gap-4 shrink-0">
                  <div className="w-24 h-24 md:w-32 md:h-32 bg-[#EAD9C6] rounded-full flex items-center justify-center text-[#4A0E0E] overflow-hidden border-4 border-[#F9F5EB]">
                    {auth.currentUser?.photoURL ? 
                      <img src={auth.currentUser.photoURL} alt="Avatar" className="w-full h-full object-cover" /> : 
                      <PersonIcon sx={{ fontSize: 80 }} className="md:!text-[80px] !text-[60px]" />
                    }
                  </div>
                  <button className="bg-[#410D0D] text-white text-[10px] uppercase font-bold py-2 px-5 rounded-full hover:bg-[#2d0909] transition-colors">
                    Change photo
                  </button>
                </div>

                <div className="flex-1 w-full space-y-4">
                  {/* Name Field */}
                  <div>
                    <label className="text-[10px] font-bold text-gray-400 uppercase ml-1">Name</label>
                    {isEditing ? (
                      <input 
                        type="text"
                        value={editName}
                        onChange={(e) => setEditName(e.target.value)}
                        className="w-full bg-[#F9F5EB] p-3 md:p-3.5 rounded-xl text-sm font-medium border-2 border-transparent focus:border-[#EAD9C6] outline-none transition-all"
                      />
                    ) : (
                      <div className="bg-[#F9F5EB] p-3 md:p-3.5 rounded-xl text-sm font-medium">{userData?.name}</div>
                    )}
                  </div>

                  {/* Pseudonym Field */}
                  <div>
                    <label className="text-[10px] font-bold text-gray-400 uppercase ml-1">Pseudonym</label>
                    {isEditing ? (
                      <input 
                        type="text"
                        value={editPseudonym}
                        onChange={(e) => setEditPseudonym(e.target.value)}
                        className="w-full bg-[#F9F5EB] p-3 md:p-3.5 rounded-xl text-sm font-medium border-2 border-transparent focus:border-[#EAD9C6] outline-none transition-all"
                      />
                    ) : (
                      <div className="bg-[#F9F5EB] p-3 md:p-3.5 rounded-xl text-sm font-medium">{userData?.pseudonym}</div>
                    )}
                  </div>

                  {/* Email Field (ReadOnly) */}
                  <div>
                    <label className="text-[10px] font-bold text-gray-400 uppercase ml-1">Email</label>
                    <div className="bg-[#F9F5EB] p-3 md:p-3.5 rounded-xl text-sm font-medium text-gray-400 italic break-all">
                      {userData?.email}
                    </div>
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 md:gap-8">
                <div className="bg-white p-6 rounded-[24px] md:rounded-[32px] shadow-sm border border-[#E8E2D2]/30 flex flex-col">
                  <h3 className="font-serif text-lg md:text-xl mb-4 text-[#433D33]">Additional data</h3>
                  <div className="mb-auto">
                    <label className="text-[10px] font-bold text-gray-400 uppercase block mb-1.5 ml-1">Member Since</label>
                    <div className="bg-[#F9F5EB] p-2 md:p-2.5 rounded-lg text-xs font-bold inline-block px-4 mb-6">{userData?.memberSince}</div>
                  </div>
                  {!isEditing && (
                    <button className="flex items-center justify-between w-full text-xs font-bold text-[#433D33] hover:text-[#4A0E0E] pt-4 border-t border-gray-50">
                      Change password <KeyboardArrowRightIcon />
                    </button>
                  )}
                </div>

                <div className="bg-white p-6 rounded-[24px] md:rounded-[32px] shadow-sm border border-[#E8E2D2]/30">
                  <h3 className="font-serif text-lg md:text-xl mb-4 md:mb-5 text-[#433D33]">Total Statistics</h3>
                  <div className="space-y-3 text-xs font-bold">
                    <div className="flex justify-between border-b border-gray-50 pb-2">
                      <span className="text-gray-400 uppercase">Books:</span><span>4</span>
                    </div>
                    <div className="flex justify-between border-b border-gray-50 pb-2">
                      <span className="text-gray-400 uppercase">Words:</span><span>142,276</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-400 uppercase">Time:</span><span>1298 hour</span>
                    </div>
                  </div>
                </div>
              </div>
              
              {/* Action Buttons */}
              <div className="flex flex-col sm:flex-row justify-end gap-3 md:gap-4 mt-2">
                {isEditing ? (
                  <>
                    <button 
                      onClick={handleCancel}
                      className="w-full sm:w-auto bg-white text-gray-500 border border-gray-200 px-6 md:px-8 py-3 md:py-3.5 rounded-2xl font-bold flex items-center justify-center gap-2 hover:bg-gray-50 active:scale-95 transition-all text-sm"
                    >
                      Cancel <CloseIcon sx={{ fontSize: 18 }} />
                    </button>
                    <button 
                      onClick={handleSave}
                      className="w-full sm:w-auto bg-[#410D0D] text-white px-6 md:px-8 py-3 md:py-3.5 rounded-2xl font-bold flex items-center justify-center gap-2 shadow-lg hover:shadow-xl active:scale-95 transition-all text-sm"
                    >
                      Save Changes <SaveIcon sx={{ fontSize: 18 }} />
                    </button>
                  </>
                ) : (
                  <button 
                    onClick={() => setIsEditing(true)}
                    className="w-full sm:w-auto bg-[#410D0D] text-white px-6 md:px-8 py-3 md:py-3.5 rounded-2xl font-bold flex items-center justify-center gap-2 shadow-lg active:scale-95 transition-transform text-sm"
                  >
                    Edit Profile <EditIcon sx={{ fontSize: 18 }} />
                  </button>
                )}
              </div>
            </div>

            {/* Right Column */}
            <div className="lg:col-span-5 flex flex-col gap-6 md:gap-8 mt-4 lg:mt-0">
              <div className="bg-white p-5 md:p-6 rounded-[24px] md:rounded-[32px] shadow-sm border border-[#E8E2D2]/30">
                <h3 className="text-gray-400 text-[10px] md:text-[11px] font-bold uppercase tracking-widest mb-4 md:mb-6">Activity Calendar</h3>
                
                <div className="flex items-center justify-between mb-4 md:mb-6 px-1 md:px-2">
                  <ChevronLeftIcon className="text-gray-300 cursor-pointer" />
                  <span className="font-bold text-xs md:text-sm">Лютий 2026</span>
                  <ChevronRightIcon className="text-gray-300 cursor-pointer" />
                </div>
                
                <div className="grid grid-cols-7 text-[9px] md:text-[10px] text-gray-300 font-bold mb-3 md:mb-4 text-center">
                  <span>S</span><span>M</span><span>T</span><span>W</span><span>T</span><span>F</span><span>S</span>
                </div>
                
                <div className="grid grid-cols-7 gap-y-2 md:gap-y-3 text-center text-[11px] md:text-xs font-bold mb-4">
                  {[1, 2, 3, 4, 5, 6, 7].map(d => <span key={d} className="py-1 text-gray-300">{d}</span>)}
                  
                  {[8, 9, 10, 11].map(d => (
                    <span key={d} className="flex justify-center py-0.5 md:py-1">
                      <div className="w-6 h-6 md:w-7 md:h-7 bg-orange-500 rounded-full text-white flex items-center justify-center shadow-sm text-[9px] md:text-xs">{d}</div>
                    </span>
                  ))}
                  
                  <span className="relative flex justify-center py-0.5 md:py-1">
                    <div className="w-6 h-6 md:w-7 md:h-7 bg-orange-500 rounded-full text-white flex items-center justify-center shadow-lg ring-2 ring-orange-200 text-[9px] md:text-xs">12</div>
                  </span>
                  
                  <span className="flex justify-center py-0.5 md:py-1">
                    <div className="w-6 h-6 md:w-7 md:h-7 bg-orange-500 rounded-full text-white flex items-center justify-center shadow-sm text-[9px] md:text-xs">13</div>
                  </span>
                  
                  {[14, 15, 16, 17, 18, 19, 20, 21, 22].map(d => <span key={d} className="py-1 text-gray-700">{d}</span>)}
                </div>
                
                <div className="mt-4 md:mt-6 pt-4 md:pt-6 border-t border-gray-50 flex justify-between items-center">
                  <span className="text-[10px] md:text-xs font-bold text-gray-400 uppercase">Current Streak:</span>
                  <span className="text-lg md:text-xl font-bold text-[#433D33]">9 Days</span>
                </div>
              </div>

              <div className="bg-white p-5 md:p-6 rounded-[24px] md:rounded-[32px] shadow-sm border border-[#E8E2D2]/30">
                <h3 className="text-gray-400 text-[10px] md:text-[11px] font-bold uppercase tracking-widest mb-4 md:mb-6">Monthly Statistics</h3>
                <div className="space-y-3 md:space-y-4 px-1 text-[11px] md:text-xs font-bold">
                  <div className="flex justify-between border-b border-gray-50 pb-2">
                    <span className="text-gray-400 uppercase">Books:</span><span>1</span>
                  </div>
                  <div className="flex justify-between border-b border-gray-50 pb-2">
                    <span className="text-gray-400 uppercase">Words:</span><span>14,478</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-400 uppercase">Time:</span><span>120 hour</span>
                  </div>
                </div>
              </div>

              {/* Delete button disappears during editing */}
              {!isEditing && (
                <div className="mt-4 md:mt-auto flex justify-center lg:justify-end">
                  <button className="w-full lg:w-auto text-[#D32F2F] border border-[#D32F2F] px-8 py-3 rounded-2xl text-xs font-bold hover:bg-red-50 active:scale-95 transition-all">
                    Delete Account
                  </button>
                </div>
              )}
            </div>
            
          </div>
        </div>
      </main>
    </div>
  );
};

export default SettingsPage;