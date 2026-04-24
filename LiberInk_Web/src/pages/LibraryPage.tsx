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
      const response = await fetch(`http://localhost:5241/api/Books/user/${uid}`);
      if (!response.ok) throw new Error('Помилка завантаження');
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
    if (!user || !newBook.title) return;
    try {
      const response = await fetch('http://localhost:5241/api/Books', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
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
    if (!window.confirm("Ви впевнені, що хочете видалити цей проект?")) return;
    try {
      const response = await fetch(`http://localhost:5241/api/Books/${id}`, { method: 'DELETE' });
      if (response.ok) setBooks(prev => prev.filter(b => b.id !== id));
    } catch (error) {
      console.error("Error deleting book:", error);
    }
  };

  if (loading) return (
    <div className="flex-1 h-full bg-[#F9F5EB] flex items-center justify-center font-serif text-[#4A0E0E]">
      Завантаження бібліотеки...
    </div>
  );

  return (
    <div className="flex-1 p-10 overflow-y-auto bg-[#F9F5EB] text-left">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-4xl font-serif text-[#433D33] font-medium mb-1">My Stories</h1>
          <p className="text-[#969188] text-sm">Manage and organize your writing projects</p>
        </div>

        {/* Search & Create Button */}
        <div className="flex gap-4 mb-10">
          <div className="flex-1 relative">
            <div className="absolute left-5 top-1/2 -translate-y-1/2 text-[#4A0E0E]/40 flex items-center">
              <SearchIcon fontSize="small" />
            </div>
            {/* 3. Прив'язка інпуту до стану searchQuery */}
            <input 
              type="text" 
              placeholder="Search your books..." 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-white border-none px-14 py-4 rounded-xl shadow-[0_2px_10px_rgba(0,0,0,0.02)] outline-none text-[#433D33] placeholder-[#C4C4C4] focus:shadow-md transition-all font-sans"
            />
          </div>
          <button 
            onClick={() => setIsModalOpen(true)}
            className="bg-[#410D0D] text-white px-8 py-4 rounded-xl font-bold flex items-center gap-2 hover:bg-[#2d0909] transition-all shadow-lg active:scale-95 whitespace-nowrap"
          >
            <AddIcon />
            <span className="text-sm uppercase tracking-wider">Create New Book</span>
          </button>
        </div>

        {/* 4. Виведення filteredBooks замість звичайного books */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 pb-10">
          {filteredBooks.length > 0 ? (
            filteredBooks.map((book, index) => (
              <div 
                key={book.id}
                onClick={() => navigate(`/editor/${book.id}`)}
                className="bg-white p-5 rounded-[22px] flex gap-5 shadow-[0_4px_15px_rgba(0,0,0,0.02)] border border-transparent hover:border-[#E8E2D2] transition-all cursor-pointer group relative overflow-hidden"
              >
                <div className="absolute top-3 right-3 flex gap-2 opacity-0 group-hover:opacity-100 transition-all transform translate-y-[-5px] group-hover:translate-y-0 z-10">
                  <button 
                    onClick={(e) => { e.stopPropagation(); navigate(`/editor/${book.id}`); }}
                    className="p-1.5 bg-[#F9F5EB] hover:bg-[#EAD9C6] rounded-md shadow-sm transition-colors text-[#4A0E0E]"
                  >
                    <EditIcon sx={{ fontSize: 18 }} />
                  </button>
                  <button 
                    onClick={(e) => handleDeleteBook(e, book.id)}
                    className="p-1.5 bg-[#F9F5EB] hover:bg-red-50 rounded-md shadow-sm text-red-600 transition-colors"
                  >
                    <DeleteIcon sx={{ fontSize: 18 }} />
                  </button>
                </div>

                <div className={`w-20 h-28 rounded-lg flex items-center justify-center p-3 text-white shadow-md shrink-0 transition-transform group-hover:rotate-1 
                  ${index % 4 === 0 ? 'bg-[#A92E2E]' : index % 4 === 1 ? 'bg-[#5B7B7B]' : index % 4 === 2 ? 'bg-[#6A826A]' : 'bg-[#566885]'}`}>
                  <span className="text-[11px] font-serif font-bold text-center leading-tight drop-shadow-sm">
                    {book.title}
                  </span>
                </div>

                <div className="flex flex-col justify-between py-1 overflow-hidden">
                  <div className="pr-6 text-left">
                    <h3 className="font-bold text-[#433D33] text-lg truncate leading-tight group-hover:text-[#4A0E0E]" title={book.title}>
                      {book.title}
                    </h3>
                    <p className="text-[12px] text-[#A09A90] mt-1.5 font-medium">Last edited: Oct 26, 2023</p>
                  </div>
                  <div className="flex items-center gap-1.5 bg-[#FEF6E7] self-start px-3 py-1.5 rounded-full border border-[#F3E3C3]">
                      <MilitaryTechIcon sx={{ fontSize: 16, color: '#A67C52' }} />
                      <span className="text-[10px] font-bold text-[#A67C52] uppercase tracking-widest">
                         {book.draftCount || 3} Drafts
                      </span>
                  </div>
                </div>
              </div>
            ))
          ) : (
            // Повідомлення, якщо нічого не знайдено
            <div className="col-span-full py-20 text-center text-[#969188]">
              Книг з такою назвою не знайдено 🖋️
            </div>
          )}
        </div>
      </div>

      {/* Modal remains the same */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-[#433D33]/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-white p-10 rounded-[40px] w-full max-w-md shadow-2xl animate-in zoom-in duration-200 text-left">
            <h2 className="text-2xl font-serif font-bold text-[#4A0E0E] mb-6">Create New Book</h2>
            <div className="space-y-4">
              <input 
                autoFocus
                className="w-full bg-[#F9F5EB] border-none p-4 rounded-xl outline-none focus:ring-2 focus:ring-[#4A0E0E]/10 font-sans" 
                placeholder="Title of your story" 
                value={newBook.title}
                onChange={(e) => setNewBook({...newBook, title: e.target.value})}
              />
              <textarea 
                className="w-full bg-[#F9F5EB] border-none p-4 rounded-xl outline-none focus:ring-2 focus:ring-[#4A0E0E]/10 resize-none font-sans" 
                placeholder="Description (optional)"
                rows={3}
                value={newBook.description}
                onChange={(e) => setNewBook({...newBook, description: e.target.value})}
              />
            </div>
            <div className="flex gap-4 mt-8">
                <button onClick={() => setIsModalOpen(false)} className="flex-1 py-4 text-[#A09A90] font-bold hover:text-gray-600 transition-colors">Cancel</button>
                <button onClick={handleCreateBook} className="flex-1 py-4 bg-[#4A0E0E] text-white rounded-xl font-bold shadow-lg hover:bg-[#2d0909]">Create</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default LibraryPage;