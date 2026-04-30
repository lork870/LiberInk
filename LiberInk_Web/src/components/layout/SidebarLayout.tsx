import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import { auth } from '../../firebase';
import my_ic_Logo from '../../assets/ic_logo.png';
import PersonIcon from '@mui/icons-material/Person';

const SidebarLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const isEditorPage = location.pathname.includes('/editor');

  const menuItems = [
    { 
      id: 'library', 
      label: isEditorPage ? 'Back' : 'Library', 
      path: '/library', 
      iconName: isEditorPage ? 'arrow_back' : 'menu_book' 
    },
    { id: 'export', label: 'Export', path: '/export', iconName: 'upload' },
    { id: 'settings', label: 'Settings', path: '/settings', iconName: 'settings' },
  ];

  return (
    <div className="flex flex-col h-screen bg-[#F9F5EB] font-sans text-[#433D33] overflow-hidden">
      
      {/* 1. УНІВЕРСАЛЬНИЙ ХЕДЕР (Завжди зверху на всю ширину) */}
      <header className="h-16 w-full bg-[#F9F5EB] border-b border-[#E8E2D2] flex items-center justify-between px-8 z-30 shrink-0">
        {!isEditorPage ? (
          <>
            <div className="flex items-center gap-3 cursor-pointer" onClick={() => navigate('/library')}>
              <img src={my_ic_Logo} alt="Logo" className="h-8 w-auto" /> 
              <span className="text-2xl font-bold font-serif text-[#4A0E0E]">LiberInk</span>
            </div>
            <div className="flex items-center gap-4">
              <button onClick={() => auth.signOut()} className="px-6 py-1.5 border border-[#4A0E0E] text-[#4A0E0E] rounded-full text-sm font-medium hover:bg-[#4A0E0E] hover:text-white transition-colors">
                Log out
              </button>
              <div className="w-9 h-9 bg-[#EAD9C6] rounded-full border-2 border-white shadow-sm flex items-center justify-center text-[#4A0E0E]">
                <PersonIcon fontSize="small" />
              </div>
            </div>
          </>
        ) : (
          <>
            <div className="flex items-center gap-4">
              <div className="w-8 h-10 bg-white rounded shadow-sm border border-gray-200 flex items-center justify-center overflow-hidden">
                 <img src="https://via.placeholder.com/40x50" alt="Cover" className="object-cover w-full h-full" />
              </div>
              <div className="text-left">
                <h2 className="font-serif font-bold text-[#4A0E0E] text-lg leading-tight">Editing Story</h2>
                <p className="text-[10px] text-gray-500 font-medium uppercase tracking-wider">Draft Mode</p>
              </div>
            </div>
            <div className="flex items-center gap-6">
              <button 
                id="global-save-btn"
                onClick={() => window.dispatchEvent(new Event('trigger-save'))}
                className="px-8 py-1.5 bg-[#F9F5EB] border border-[#4A0E0E] text-[#4A0E0E] rounded-full font-bold text-sm hover:bg-[#4A0E0E] hover:text-white transition-all shadow-sm"
              >
                Save
              </button>
              <div className="w-10 h-10 bg-[#EAD9C6] rounded-full border border-white shadow-sm flex items-center justify-center text-[#4A0E0E]">
                <PersonIcon />
              </div>
            </div>
          </>
        )}
      </header>

      {/* НИЖНЯ ЧАСТИНА: SIDEBAR + MAIN */}
      <div className="flex flex-1 overflow-hidden">
        
        {/* 2. ЄДИНИЙ САЙДБАР (Тепер він завжди йде до самого низу екрана) */}
        <aside className="w-24 bg-[#F9F5EB] border-r border-[#E8E2D2] flex flex-col items-center py-4 shrink-0 z-20 relative text-center">
          {menuItems.map((item) => {
            const isActive = location.pathname.startsWith(item.path);
            const isBackBtn = item.id === 'library' && isEditorPage;
            
            return (
              <div
                key={item.id}
                onClick={() => navigate(item.path)}
                className="w-full flex flex-col items-center py-3 cursor-pointer relative group"
              >
                <div className="w-full h-12 flex items-center justify-center relative">
                  {isActive && !isBackBtn && (
                    <>
                      <motion.div layoutId="activePill" className="absolute inset-y-0 inset-x-0 bg-[#F3EAD3] z-0" transition={{ type: 'spring', stiffness: 300, damping: 30 }} />
                      <motion.div layoutId="activeBorder" className="absolute right-0 top-0 bottom-0 w-[3px] bg-[#410D0D] z-10" transition={{ type: 'spring', stiffness: 300, damping: 30 }} />
                    </>
                  )}
                  <span 
                    className="material-symbols-rounded relative z-10 transition-all duration-300 select-none"
                    style={{ 
                      fontSize: '26px',
                      color: (isActive || isBackBtn) ? '#4A0E0E' : '#9ca3af',
                      fontVariationSettings: (isActive && !isBackBtn) ? "'FILL' 1" : "'FILL' 0" 
                    }}
                  >
                    {item.iconName}
                  </span>
                </div>
                <span className={`mt-1 text-[11px] font-bold tracking-tight uppercase transition-colors duration-300 ${isActive ? "text-[#410D0D]" : "text-gray-400 opacity-60 group-hover:text-[#4A0E0E]"}`}>
                  {item.label}
                </span>
              </div>
            );
          })}
        </aside>

        {/* 3. ЗОНА КОНТЕНТУ (Library або Editor) */}
        <main className="flex-1 overflow-hidden relative flex flex-col bg-[#F9F5EB]">
            <Outlet /> 
        </main>
      </div>
    </div>
  );
};

export default SidebarLayout;