import React, { useEffect, useState } from 'react';
import { auth } from '../firebase';
import { useNavigate } from 'react-router-dom';

// Імпорт іконок Material Design
import SearchIcon from '@mui/icons-material/Search';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import MilitaryTechIcon from '@mui/icons-material/MilitaryTech';

interface Book {
  id: number;
  title: string;
  description: string;
  userId: string;
  authorPseudonym: string;
  lastEdited: string;
  draftCount?: number;
}

const LibraryPage = () => {
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newBook, setNewBook] = useState({ title: '', description: '' });
  
  // 1. Стан для пошукового запиту
  const [searchQuery, setSearchQuery] = useState('');
  
  const navigate = useNavigate();

  useEffect(() => {
    const unsubscribe = auth.onAuthStateChanged((user) => {
      if (user) {
        fetchBooks(user.uid);
      } else {
        navigate('/login');
      }
    });
    
    return () => unsubscribe();
  }, [navigate]);

  const fetchBooks = async (uid: string) => {
    try {
      const response = await fetch(`https://liber-ink-api.onrender.com/api/Books/user/${uid}`);
      
      if (!response.ok) {
        throw new Error('Помилка завантаження');
      }
      
      const data = await response.json();
      setBooks(data);
      
    } catch (error) {
      console.error("API Error:", error);
    } finally {
      setLoading(false);
    }
  };

  // 2. Фільтрація книг на основі пошукового запиту
  const filteredBooks = books.filter((book) =>
    book.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
    book.description.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleCreateBook = async () => {
    const user = auth.currentUser;
    
    if (!user || !newBook.title) {
      return;
    }
    
    try {
      const response = await fetch('https://liber-ink-api.onrender.com/api/Books', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json' 
        },
        body: JSON.stringify({
          title: newBook.title,
          description: newBook.description,
          userId: user.uid,
          authorPseudonym: user.displayName || "Author",
          lastEdited: new Date().toISOString()
        })
      });
      
      if (response.ok) {
        const createdBook = await response.json();
        setIsModalOpen(false);
        setNewBook({ title: '', description: '' });
        navigate(`/editor/${createdBook.id}`);
      }
    } catch (error) {
      console.error("Error creating book:", error);
    }
  };

  const handleDeleteBook = async (e: React.MouseEvent, id: number) => {
    e.stopPropagation();
    
    if (!window.confirm("Ви впевнені, що хочете видалити цей проект?")) {
      return;
    }
    
    try {
      const response = await fetch(`https://liber-ink-api.onrender.com/api/Books/${id}`, { 
        method: 'DELETE' 
      });
      
      if (response.ok) {
        setBooks(prev => prev.filter(b => b.id !== id));
      }
    } catch (error) {
      console.error("Error deleting book:", error);
    }
  };

  if (loading) {
    return (
      <div className="flex-1 h-full bg-[#F9F5EB] flex items-center justify-center font-serif text-[#4A0E0E]">
        Завантаження бібліотеки...
      </div>
    );
  }

  return (
    <div className="flex-1 p-4 md:p-10 overflow-y-auto bg-[#F9F5EB] text-left">
      <div className="max-w-7xl mx-auto">
        
        <div className="mb-6 md:mb-8">
          <h1 className="text-3xl md:text-4xl font-serif text-[#433D33] font-medium mb-1">
            My Stories
          </h1>
          <p className="text-[#969188] text-xs md:text-sm">
            Manage and organize your writing projects
          </p>
        </div>

        {/* Search & Create Button */}
        <div className="flex flex-col md:flex-row gap-3 md:gap-4 mb-8 md:mb-10">
          <div className="flex-1 relative w-full">
            <div className="absolute left-4 md:left-5 top-1/2 -translate-y-1/2 text-[#4A0E0E]/40 flex items-center">
              <SearchIcon fontSize="small" />
            </div>
            
            {/* 3. Прив'язка інпуту до стану searchQuery */}
            <input 
              type="text" 
              placeholder="Search your books..." 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-white border-none pl-12 md:pl-14 pr-4 py-3 md:py-4 rounded-xl shadow-[0_2px_10px_rgba(0,0,0,0.02)] outline-none text-[#433D33] placeholder-[#C4C4C4] focus:shadow-md transition-all font-sans text-sm md:text-base"
            />
          </div>
          
          <button 
            onClick={() => setIsModalOpen(true)}
            className="w-full md:w-auto bg-[#410D0D] text-white px-6 md:px-8 py-3 md:py-4 rounded-xl font-bold flex items-center justify-center gap-2 hover:bg-[#2d0909] transition-all shadow-lg active:scale-95 whitespace-nowrap"
          >
            <AddIcon />
            <span className="text-xs md:text-sm uppercase tracking-wider">
              Create New Book
            </span>
          </button>
        </div>

        {/* 4. Виведення filteredBooks замість звичайного books */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 md:gap-6 pb-10">
          {filteredBooks.length > 0 ? (
            filteredBooks.map((book, index) => (
              <div 
                key={book.id}
                onClick={() => navigate(`/editor/${book.id}`)}
                className="bg-white p-4 md:p-5 rounded-[22px] flex gap-4 md:gap-5 shadow-[0_4px_15px_rgba(0,0,0,0.02)] border border-transparent hover:border-[#E8E2D2] transition-all cursor-pointer group relative overflow-hidden"
              >
                
                {/* Кнопки редагування: на мобільному завжди видимі, на ПК з'являються при наведенні */}
                <div className="absolute top-2 md:top-3 right-2 md:right-3 flex gap-1.5 md:gap-2 opacity-100 md:opacity-0 md:group-hover:opacity-100 transition-all transform md:translate-y-[-5px] md:group-hover:translate-y-0 z-10">
                  <button 
                    onClick={(e) => { 
                      e.stopPropagation(); 
                      navigate(`/editor/${book.id}`); 
                    }}
                    className="p-1 md:p-1.5 bg-[#F9F5EB]/80 md:bg-[#F9F5EB] hover:bg-[#EAD9C6] rounded-md shadow-sm transition-colors text-[#4A0E0E] backdrop-blur-sm md:backdrop-blur-none"
                  >
                    <EditIcon sx={{ fontSize: 16 }} className="md:!text-[18px]" />
                  </button>
                  <button 
                    onClick={(e) => handleDeleteBook(e, book.id)}
                    className="p-1 md:p-1.5 bg-[#F9F5EB]/80 md:bg-[#F9F5EB] hover:bg-red-50 rounded-md shadow-sm text-red-600 transition-colors backdrop-blur-sm md:backdrop-blur-none"
                  >
                    <DeleteIcon sx={{ fontSize: 16 }} className="md:!text-[18px]" />
                  </button>
                </div>

                <div className={`w-16 h-24 md:w-20 md:h-28 rounded-lg flex items-center justify-center p-2 md:p-3 text-white shadow-md shrink-0 transition-transform md:group-hover:rotate-1 
                  ${index % 4 === 0 ? 'bg-[#A92E2E]' : 
                    index % 4 === 1 ? 'bg-[#5B7B7B]' : 
                    index % 4 === 2 ? 'bg-[#6A826A]' : 
                    'bg-[#566885]'}`
                }>
                  <span className="text-[9px] md:text-[11px] font-serif font-bold text-center leading-tight drop-shadow-sm line-clamp-4">
                    {book.title}
                  </span>
                </div>

                <div className="flex flex-col justify-between py-1 overflow-hidden flex-1">
                  <div className="pr-12 md:pr-6 text-left">
                    <h3 
                      className="font-bold text-[#433D33] text-base md:text-lg truncate leading-tight md:group-hover:text-[#4A0E0E]" 
                      title={book.title}
                    >
                      {book.title}
                    </h3>
                    <p className="text-[11px] md:text-[12px] text-[#A09A90] mt-1 md:mt-1.5 font-medium">
                      Last edited: {new Date(book.lastEdited || new Date()).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                    </p>
                  </div>
                  
                  <div className="flex items-center gap-1 md:gap-1.5 bg-[#FEF6E7] self-start px-2 md:px-3 py-1 md:py-1.5 rounded-full border border-[#F3E3C3] mt-2 md:mt-0">
                      <MilitaryTechIcon sx={{ fontSize: 14 }} className="md:!text-[16px] !text-[#A67C52]" />
                      <span className="text-[9px] md:text-[10px] font-bold text-[#A67C52] uppercase tracking-widest">
                         {book.draftCount || 3} Drafts
                      </span>
                  </div>
                </div>

              </div>
            ))
          ) : (
            <div className="col-span-full py-16 md:py-20 text-center text-[#969188] text-sm md:text-base">
              Книг з такою назвою не знайдено 🖋️
            </div>
          )}
        </div>
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-[100] flex items-start md:items-center justify-center p-4 md:p-6">
          {/* Backdrop (фон, що затемнює) */}
          <div 
            className="absolute inset-0 bg-[#433D33]/40 backdrop-blur-sm animate-in fade-in duration-200"
            onClick={() => setIsModalOpen(false)}
          ></div>

          {/* Модальна картка */}
          <div className="bg-white w-full max-w-md rounded-[32px] md:rounded-[40px] shadow-2xl animate-in zoom-in-95 duration-200 relative z-10 my-auto overflow-hidden">
            
            {/* Контейнер для скролу (на випадок низької висоти екрану) */}
            <div className="p-6 md:p-10 max-h-[85vh] overflow-y-auto">
              <h2 className="text-xl md:text-2xl font-serif font-bold text-[#4A0E0E] mb-4 md:mb-6">
                Create New Book
              </h2>
              
              <div className="space-y-3 md:space-y-4">
                <input 
                  autoFocus
                  className="w-full bg-[#F9F5EB] border-none p-3 md:p-4 rounded-xl outline-none focus:ring-2 focus:ring-[#4A0E0E]/20 font-sans text-sm md:text-base transition-all" 
                  placeholder="Title of your story" 
                  value={newBook.title}
                  onChange={(e) => setNewBook({...newBook, title: e.target.value})}
                />
                
                <textarea 
                  className="w-full bg-[#F9F5EB] border-none p-3 md:p-4 rounded-xl outline-none focus:ring-2 focus:ring-[#4A0404]/20 resize-none font-sans text-sm md:text-base transition-all" 
                  placeholder="Description (optional)"
                  rows={3}
                  value={newBook.description}
                  onChange={(e) => setNewBook({...newBook, description: e.target.value})}
                />
              </div>
              
              <div className="flex gap-3 md:gap-4 mt-6 md:mt-8">
                  <button 
                    onClick={() => setIsModalOpen(false)} 
                    className="flex-1 py-3 md:py-4 text-[#A09A90] font-bold hover:text-gray-600 transition-colors text-sm md:text-base active:scale-95 transition-transform"
                  >
                    Cancel
                  </button>
                  
                  <button 
                    onClick={handleCreateBook} 
                    className="flex-1 py-3 md:py-4 bg-[#4A0E0E] text-white rounded-xl font-bold shadow-lg hover:bg-[#2d0909] text-sm md:text-base active:scale-95 transition-transform"
                  >
                    Create
                  </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default LibraryPage;