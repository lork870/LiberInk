import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import TextAlign from '@tiptap/extension-text-align';
import { PageBreak } from './PageBreak';

// Імпорти іконок
import {
  FormatBold, FormatItalic, FormatUnderlined,
  StrikethroughS, FormatAlignLeft, FormatAlignCenter,
  FormatAlignRight, FormatAlignJustify, Add,
  CheckCircleOutlined, FolderOpen, KeyboardArrowDown,
  InsertDriveFile, HorizontalRule
} from '@mui/icons-material';

interface Book {
  id: number;
  title: string;
  description: string;
  userId: string;
  authorPseudonym: string;
  lastEdited: string;
}

// 1. КОНФІГУРАЦІЯ ФОРМАТІВ СТОРІНОК (Розміри в пікселях)
const PAGE_FORMATS = {
  A4: { width: 794, height: 1123, name: 'A4' },
  A5: { width: 559, height: 794, name: 'A5' },
  Letter: { width: 816, height: 1056, name: 'Letter' },
  Pocket: { width: 416, height: 605, name: 'Pocket (Кишеньковий)' },
};

type FormatKey = keyof typeof PAGE_FORMATS;

const EditorPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [book, setBook] = useState<Book | null>(null);
  const [zoom, setZoom] = useState(100);
  const [wordCount, setWordCount] = useState(0);
  const [pageCount, setPageCount] = useState(1); 
  
  // Стан для поточного формату
  const [currentFormat, setCurrentFormat] = useState<FormatKey>('A4');
  // useRef потрібен, щоб onUpdate завжди бачив актуальний формат без перерендеру редактора
  const currentFormatRef = useRef<FormatKey>(currentFormat);
  
  const editorContainerRef = useRef<HTMLDivElement>(null);

  // Оновлюємо ref при зміні формату
  useEffect(() => {
    currentFormatRef.current = currentFormat;
  }, [currentFormat]);

  // 2. Налаштування Tiptap
  const editor = useEditor({
    extensions: [
      StarterKit,
      Underline,
      TextAlign.configure({ types: ['heading', 'paragraph'] }),
      PageBreak, 
    ],
    editorProps: {
      attributes: {
        // Прибрали жорстку висоту (min-h-[943px]), тепер контейнер диктує розмір
        class: 'outline-none font-serif leading-[2] text-[18px] text-[#333] text-justify h-full', 
      },
    },
    onUpdate: ({ editor }) => {
      const text = editor.getText();
      setWordCount(text.trim() ? text.trim().split(/\s+/).length : 0);

      // Вираховуємо кількість сторінок на основі АКТУАЛЬНОГО формату
      const pageHeight = PAGE_FORMATS[currentFormatRef.current].height;
      const contentHeight = editor.view.dom.scrollHeight + 180; // 180 - це padding (90 зверху + 90 знизу)
      const calculatedPages = Math.max(1, Math.ceil(contentHeight / pageHeight));
      
      if (calculatedPages !== pageCount) {
        setPageCount(calculatedPages);
      }
    },
  });

  // Перерахунок сторінок при зміні формату
  useEffect(() => {
    if (editor) {
      const pageHeight = PAGE_FORMATS[currentFormat].height;
      const contentHeight = editor.view.dom.scrollHeight + 180;
      setPageCount(Math.max(1, Math.ceil(contentHeight / pageHeight)));
    }
  }, [currentFormat, editor]);

  // 3. Завантаження даних
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

  // 4. Професійний Zoom (Pinch + Mouse Wheel)
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
          <div className="sticky top-4 bg-white/95 backdrop-blur-md px-4 py-2 rounded-2xl shadow-xl border border-[#E8E2D2]/50 flex items-center gap-1 mb-10 z-50 transition-all">
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
            
            <div className="w-px h-6 bg-gray-200 mx-2" />
            
            {/* КНОПКА РОЗРИВУ СТОРІНКИ */}
            <button 
              onMouseDown={(e) => { 
                e.preventDefault(); 
                // @ts-ignore
                editor.chain().focus().setPageBreak().run(); 
              }}
              className="p-2 rounded-lg transition-all text-gray-500 hover:bg-[#F3EAD3] hover:text-[#4A0E0E]"
              title="Додати розрив сторінки (Ctrl+Enter)"
            >
              <HorizontalRule sx={{ fontSize: 20 }} />
            </button>

            <div className="w-px h-6 bg-gray-200 mx-2" />

            {/* ПЕРЕМИКАЧ ФОРМАТІВ СТОРІНОК */}
            <select 
              value={currentFormat}
              onChange={(e) => setCurrentFormat(e.target.value as FormatKey)}
              title="Формат аркуша"
              className="bg-transparent text-[12px] font-bold text-[#4A0E0E] outline-none cursor-pointer hover:bg-[#F3EAD3] px-2 py-2 rounded-lg transition-all"
            >
              {Object.keys(PAGE_FORMATS).map((key) => (
                <option key={key} value={key} className="font-sans text-gray-800">
                  {PAGE_FORMATS[key as FormatKey].name}
                </option>
              ))}
            </select>

          </div>

          {/* ВЕРТИКАЛЬНІ СТОРІНКИ (Динамічний розмір) */}
          <div 
            className="transition-transform duration-100 origin-top pb-40 flex flex-col items-center gap-10"
            style={{ transform: `scale(${zoom / 100})` }}
          >
            <div className="relative shadow-2xl border border-gray-200 bg-white flex transition-all duration-300 ease-in-out"
                 style={{ 
                   width: `${PAGE_FORMATS[currentFormat].width}px`, 
                   minHeight: `${PAGE_FORMATS[currentFormat].height}px` 
                 }}>
              
              {/* ЛІВА ЛІНІЙКА */}
              <div className="w-16 shrink-0 border-r border-[#E8E2D2] bg-[#fdfcf9] relative overflow-hidden pointer-events-none">
                {Array.from({ length: pageCount }).map((_, i) => (
                  <div 
                    key={i} 
                    className="absolute w-full flex flex-col items-center transition-all duration-300 ease-in-out"
                    style={{ top: `${i * PAGE_FORMATS[currentFormat].height}px` }} 
                  >
                    <span className="mt-4 text-[10px] font-bold text-gray-400">
                      Стор. {i + 1}
                    </span>
                    {i > 0 && (
                      <div className="absolute top-0 w-full border-t-2 border-dashed border-gray-300"></div>
                    )}
                  </div>
                ))}
              </div>

              <div 
                className="flex-1" 
                style={{ 
                  padding: currentFormat === 'Pocket' ? '40px 30px' : '90px 54px', 
                  width: '100%', 
                  maxWidth: `${PAGE_FORMATS[currentFormat].width}px`,
                  boxSizing: 'border-box',
                  display: 'flex',
                  flexDirection: 'column'
                }}
              >
                <EditorContent 
                  editor={editor} 
                  className="h-full w-full" 
                  style={{
                    boxSizing: 'border-box',
                    // Ці властивості змусять текст (і код) переноситися, не ламаючи правий відступ
                    wordBreak: 'break-word', 
                    overflowWrap: 'anywhere',
                    textAlign: 'justify', // Можна залишити, якщо слова будуть переноситись
                  }}
                />
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