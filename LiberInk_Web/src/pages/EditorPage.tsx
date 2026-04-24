import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import TextAlign from '@tiptap/extension-text-align';

// Імпорти іконок
import {
  FormatBold, FormatItalic, FormatUnderlined,
  StrikethroughS, FormatAlignLeft, FormatAlignCenter,
  FormatAlignRight, FormatAlignJustify, Add,
  CheckCircleOutlined, FolderOpen, KeyboardArrowDown,
  InsertDriveFile
} from '@mui/icons-material';

interface Book {
  id: number;
  title: string;
  description: string;
  userId: string;
  authorPseudonym: string;
  lastEdited: string;
}

const EditorPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [book, setBook] = useState<Book | null>(null);
  const [zoom, setZoom] = useState(100);
  const [wordCount, setWordCount] = useState(0);
  const [pageCount, setPageCount] = useState(1);
  const editorContainerRef = useRef<HTMLDivElement>(null);

  // 1. Налаштування Tiptap
  const editor = useEditor({
    extensions: [
      StarterKit,
      Underline,
      TextAlign.configure({ types: ['heading', 'paragraph'] }),
    ],
    editorProps: {
      attributes: {
        // Прибираємо фіксовану висоту з самого редактора, 
        // він буде рости всередині контейнера сторінок
        class: 'outline-none font-serif leading-[2] text-[18px] text-[#333] text-justify min-h-[1000px]',
      },
    },
    onUpdate: ({ editor }) => {
      const text = editor.getText();
      setWordCount(text.trim() ? text.trim().split(/\s+/).length : 0);
      
      // Розрахунок сторінок: ми вимірюємо висоту DOM-елемента редактора
      // і ділимо на висоту віртуальної сторінки (приблизно 1056px)
      const contentHeight = editor.view.dom.scrollHeight;
      const calculatedPages = Math.max(1, Math.ceil(contentHeight / 1056));
      setPageCount(calculatedPages);
    },
  });

  // 2. Завантаження даних
  useEffect(() => {
    const fetchBook = async () => {
      try {
        const response = await fetch(`http://localhost:5241/api/Books/${id}`);
        if (!response.ok) throw new Error("Книгу не знайдено");
        const data = await response.json();
        setBook(data);
        if (editor && data.description) {
          editor.commands.setContent(data.description);
        }
      } catch (err) {
        navigate('/library');
      }
    };
    fetchBook();
  }, [id, editor, navigate]);

  // 3. Професійний Zoom (Pinch + Mouse Wheel)
  useEffect(() => {
    const handleWheel = (e: WheelEvent) => {
      if (e.ctrlKey || e.metaKey) {
        e.preventDefault();
        const delta = e.deltaY * 0.4;
        setZoom((prev) => Math.min(Math.max(prev - delta, 30), 200));
      }
    };
    window.addEventListener('wheel', handleWheel, { passive: false });
    return () => window.removeEventListener('wheel', handleWheel);
  }, []);

  // 4. Логіка збереження
  useEffect(() => {
    const saveBtn = document.getElementById('global-save-btn');
    const handleSave = async () => {
      if (!book || !editor) return;
      try {
        await fetch(`http://localhost:5241/api/Books/${id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ 
            ...book, 
            description: editor.getHTML(), 
            lastEdited: new Date().toISOString() 
          })
        });
      } catch (err) {
        console.error(err);
      }
    };
    saveBtn?.addEventListener('click', handleSave);
    return () => saveBtn?.removeEventListener('click', handleSave);
  }, [book, editor, id]);

  if (!editor || !book) return null;

  return (
    <div className="flex flex-col flex-1 h-full overflow-hidden bg-[#F9F5EB]">
      <div className="flex flex-1 overflow-hidden pt-4">
        
        {/* ЛІВА ПАНЕЛЬ */}
        <aside className="w-64 bg-white rounded-r-[32px] border border-[#E8E2D2]/50 flex flex-col shrink-0 mb-4 shadow-sm">
          <div className="p-6 text-left">
            <h3 className="font-serif text-xl font-bold mb-6 text-[#433D33]">Structure</h3>
            <div className="flex items-center gap-2 py-2 font-bold text-sm text-[#4A0E0E]">
              <KeyboardArrowDown sx={{ fontSize: 18 }} />
              <FolderOpen sx={{ fontSize: 18 }} />
              <span>{book.title}</span>
            </div>
            <div className="ml-6 space-y-1">
               <div className="flex items-center gap-3 py-2 px-3 bg-[#F3EAD3] rounded-xl text-[#410D0D] font-bold text-xs shadow-sm">
                  <InsertDriveFile sx={{ fontSize: 16 }} />
                  <span>Main Draft</span>
               </div>
            </div>
          </div>
        </aside>

        {/* ЦЕНТРАЛЬНА РОБОЧА ЗОНА */}
        <main 
          ref={editorContainerRef} 
          className="flex-1 overflow-y-auto flex flex-col items-center scrollbar-hide bg-[#EAD9C6]/20 relative pt-4"
        >
          {/* TOOLBAR */}
          <div className="sticky top-4 bg-white/95 backdrop-blur-md px-4 py-2 rounded-2xl shadow-xl border border-[#E8E2D2]/50 flex items-center gap-1 mb-10 z-50">
            <ToolbarButton 
              active={editor.isActive('bold')} 
              onClick={() => editor.chain().focus().toggleBold().run()} 
              IconComponent={FormatBold} 
            />
            <ToolbarButton 
              active={editor.isActive('italic')} 
              onClick={() => editor.chain().focus().toggleItalic().run()} 
              IconComponent={FormatItalic} 
            />
            <ToolbarButton 
              active={editor.isActive('underline')} 
              onClick={() => editor.chain().focus().toggleUnderline().run()} 
              IconComponent={FormatUnderlined} 
            />
            <ToolbarButton 
              active={editor.isActive('strike')} 
              onClick={() => editor.chain().focus().toggleStrike().run()} 
              IconComponent={StrikethroughS} 
            />
            <div className="w-px h-6 bg-gray-200 mx-2" />
            <ToolbarButton 
              active={editor.isActive({ textAlign: 'left' })} 
              onClick={() => editor.chain().focus().setTextAlign('left').run()} 
              IconComponent={FormatAlignLeft} 
            />
            <ToolbarButton 
              active={editor.isActive({ textAlign: 'center' })} 
              onClick={() => editor.chain().focus().setTextAlign('center').run()} 
              IconComponent={FormatAlignCenter} 
            />
            <ToolbarButton 
              active={editor.isActive({ textAlign: 'right' })} 
              onClick={() => editor.chain().focus().setTextAlign('right').run()} 
              IconComponent={FormatAlignRight} 
            />
            <ToolbarButton 
              active={editor.isActive({ textAlign: 'justify' })} 
              onClick={() => editor.chain().focus().setTextAlign('justify').run()} 
              IconComponent={FormatAlignJustify} 
            />
          </div>

          {/* ВЕРТИКАЛЬНІ СТОРІНКИ */}
          <div 
            className="transition-transform duration-75 origin-top pb-40 flex flex-col items-center gap-10"
            style={{ transform: `scale(${zoom / 100})` }}
          >
            {/* ПРОФЕСІЙНИЙ ПІДХІД: 
                Ми рендеримо один EditorContent, але накладаємо поверх нього 
                контейнери, які візуально розбивають його на А4.
            */}
            <div className="relative shadow-2xl border border-gray-200 bg-white"
                 style={{ 
                   width: '816px', 
                   minHeight: '1123px',
                 }}>
              
              {/* Візуальні розділювачі сторінок */}
              {Array.from({ length: Math.max(0, pageCount - 1) }).map((_, i) => (
                <div 
                  key={i}
                  className="absolute left-0 right-0 z-20 pointer-events-none flex flex-col items-center"
                  style={{ top: `${(i + 1) * 1123}px`, transform: 'translateY(-20px)' }}
                >
                  <div className="w-full h-10 bg-[#F5F0E5] border-y border-[#E8E2D2] flex items-center justify-center">
                    <span className="text-[9px] font-bold text-gray-400 tracking-widest uppercase">
                      Page Break • {i + 2}
                    </span>
                  </div>
                </div>
              ))}

              <div className="relative z-10" style={{ padding: '90px 100px' }}>
                <EditorContent editor={editor} />
              </div>
            </div>
          </div>
        </main>

        {/* ПРАВА ПАНЕЛЬ */}
        <aside className="w-80 bg-white rounded-l-[32px] border border-[#E8E2D2]/50 flex flex-col shrink-0 mb-4 shadow-sm overflow-hidden">
          <div className="p-6">
            <h4 className="font-bold text-xs mb-4 text-gray-400 uppercase tracking-widest">Notes</h4>
            <div className="bg-green-50 p-4 rounded-2xl border border-green-100 mb-4 relative">
                <CheckCircleOutlined className="text-green-500 absolute top-4 right-4" sx={{ fontSize: 18 }} />
                <span className="text-[10px] font-bold text-green-600 block mb-1 uppercase">Comment</span>
                <p className="text-xs text-green-800 leading-relaxed pr-6">Тут все чудово!</p>
            </div>
            <button className="w-full py-4 border-2 border-dashed border-gray-100 rounded-2xl text-gray-300 hover:text-orange-400 transition-all">
              <Add />
            </button>
          </div>
        </aside>
      </div>

      {/* FOOTER */}
      <footer className="h-12 bg-[#F9F5EB] border-t border-[#E8E2D2] flex items-center justify-between px-10 shrink-0 z-20 font-sans text-[11px] font-bold text-[#433D33]">
        <div className="flex gap-10">
           <span>PAGES: {pageCount}</span>
           <span>WORDS: {wordCount}</span>
        </div>
        <div className="flex items-center gap-4 w-64">
           <input type="range" min="30" max="200" value={zoom} onChange={(e) => setZoom(Number(e.target.value))} 
                  className="flex-1 h-1 bg-gray-300 appearance-none accent-[#4A0E0E] cursor-pointer" />
           <span className="min-w-[35px] text-right">{Math.round(zoom)}%</span>
        </div>
      </footer>
    </div>
  );
};

interface ToolbarButtonProps {
  active: boolean;
  onClick: () => void;
  IconComponent: React.ElementType;
}

const ToolbarButton = ({ active, onClick, IconComponent }: ToolbarButtonProps) => (
  <button 
    onMouseDown={(e) => { e.preventDefault(); onClick(); }}
    className={`p-2 rounded-lg transition-all ${
      active ? 'bg-[#4A0E0E] text-white shadow-md' : 'text-gray-500 hover:bg-gray-100'
    }`}
  >
    <IconComponent sx={{ fontSize: 20 }} />
  </button>
);

export default EditorPage;