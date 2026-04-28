import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import TextAlign from '@tiptap/extension-text-align';
import { TextStyle } from '@tiptap/extension-text-style';
import { FontSize } from './FontSize'; // Твоє кастомне розширення
import { PageBreak } from './PageBreak';
import FontFamily from '@tiptap/extension-font-family';

// Імпорти іконок Material UI
import {
  FormatBold,
  FormatItalic,
  FormatUnderlined,
  StrikethroughS,
  FormatAlignLeft,
  FormatAlignCenter,
  FormatAlignRight,
  FormatAlignJustify,
  FormatListBulleted,
  FormatListNumbered,
  Add,
  CheckCircleOutlined,
  FolderOpen,
  KeyboardArrowDown,
  InsertDriveFile,
  HorizontalRule
} from '@mui/icons-material';

interface Book {
  id: number;
  title: string;
  description: string;
  userId: string;
  authorPseudonym: string;
  lastEdited: string;
}

const FONTS = [
  { name: 'Garamond', family: '"EB Garamond", serif', type: 'Книжкова класика' },
  { name: 'Merriweather', family: 'Merriweather, serif', type: 'Комфортне читання' },
  { name: 'Lora', family: 'Lora, serif', type: 'Сучасна проза' },
  { name: 'Courier Prime', family: '"Courier Prime", monospace', type: 'Друкарська машинка' },
  { name: 'Inter', family: 'Inter, sans-serif', type: 'Сучасний стиль' },
];

const PAGE_FORMATS = {
  A4: { width: 794, height: 1123, name: 'A4', sub: '21 cm x 29,7 cm' },
  A5: { width: 559, height: 794, name: 'A5', sub: '14,8 cm x 21 cm' },
  Letter: { width: 816, height: 1056, name: 'Letter', sub: '21,59 cm x 27,94 cm' },
  Pocket: { width: 416, height: 605, name: 'Pocket', sub: '11 cm x 15 cm' },
};

const FONT_SIZES = ['8', '10', '12', '14', '16', '18', '20', '24', '30', '36', '48', '60', '72'];

type FormatKey = keyof typeof PAGE_FORMATS;

const EditorPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [book, setBook] = useState<Book | null>(null);
  const [zoom, setZoom] = useState(100);
  const [wordCount, setWordCount] = useState(0);
  const [pageCount, setPageCount] = useState(1);
  const [currentFormat, setCurrentFormat] = useState<FormatKey>('A4');
  
  const [fontSizeInput, setFontSizeInput] = useState('11');
  const [showSizeDropdown, setShowSizeDropdown] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const [, setUpdateTick] = useState(0);
  const forceUpdate = useCallback(() => setUpdateTick(tick => tick + 1), []);

  const [showFontDropdown, setShowFontDropdown] = useState(false);
  const fontDropdownRef = useRef<HTMLDivElement>(null);

  const handleFontChange = (family: string) => {
    editor?.chain().focus().setFontFamily(family).run();
    setShowFontDropdown(false);
  };

  const currentFormatRef = useRef<FormatKey>(currentFormat);
  const editorContainerRef = useRef<HTMLDivElement>(null);

  const [showFormatDropdown, setShowFormatDropdown] = useState(false);
  const formatDropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as Node;
      if (dropdownRef.current && !dropdownRef.current.contains(target)) {
        setShowSizeDropdown(false);
      }
      if (fontDropdownRef.current && !fontDropdownRef.current.contains(target)) {
        setShowFontDropdown(false);
      }
      if (formatDropdownRef.current && !formatDropdownRef.current.contains(target)) {
        setShowFormatDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // 2. НАЛАШТУВАННЯ TIPTAP РЕДАКТОРА
  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        bulletList: { keepMarks: true, keepAttributes: false },
        orderedList: { keepMarks: true, keepAttributes: false },
      }),
      Underline,
      TextStyle,
      FontSize,
      FontFamily,
      TextAlign.configure({ 
        types: ['heading', 'paragraph', 'bulletList', 'orderedList'], 
      }),
      PageBreak, 
    ],
    editorProps: {
      attributes: {
        // ProseMirror — базовий клас для стилізації
        class: 'outline-none ProseMirror h-full min-h-[500px]',
      },
    },
    onUpdate: ({ editor }) => {
      // Підрахунок слів
      const text = editor.getText();
      setWordCount(text.trim() ? text.trim().split(/\s+/).length : 0);

      // Розрахунок сторінок
      const pageHeight = PAGE_FORMATS[currentFormatRef.current].height;
      const contentHeight = editor.view.dom.scrollHeight + 180; 
      const calculatedPages = Math.max(1, Math.ceil(contentHeight / pageHeight));
      
      if (calculatedPages !== pageCount) {
        setPageCount(calculatedPages);
      }

      const attrs = editor.getAttributes('textStyle');
      const size = attrs.fontSize ? attrs.fontSize.replace('pt', '') : '14';
      setFontSizeInput(size);

      forceUpdate();
    },
    // Оновлення UI при кліках та виділенні
    onSelectionUpdate: ({ editor }) => {
      const attrs = editor.getAttributes('textStyle');
      const size = attrs.fontSize ? attrs.fontSize.replace('pt', '') : '14';
      setFontSizeInput(size);

      forceUpdate();
    },
    onTransaction: forceUpdate,
  });

  const handleFontSizeChange = (value: string) => {
  setFontSizeInput(value);
  const size = parseInt(value);
  if (!isNaN(size) && size > 0 && size < 200) {
    // Використовуємо 'pt' — це стандарт для друкованих документів та Word
    editor?.chain().focus().setFontSize(`${size}pt`).run();
  }
  setShowSizeDropdown(false);
};

  // Перехоплення Enter в інпуті
  const handleFontSizeKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleFontSizeChange(fontSizeInput);
      editor?.commands.focus();
    }
  };

  // Перерахунок сторінок при зміні формату аркуша
  useEffect(() => {
    if (editor) {
      const pageHeight = PAGE_FORMATS[currentFormat].height;
      const contentHeight = editor.view.dom.scrollHeight + 180;
      setPageCount(Math.max(1, Math.ceil(contentHeight / pageHeight)));
    }
  }, [currentFormat, editor]);

  // 3. ЗАВАНТАЖЕННЯ ДАНИХ З API
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

  // 4. ПРОФЕСІЙНИЙ ZOOM (Ctrl + Wheel)
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

  const WordSizeIcon = () => (
    <svg 
      className="absolute inset-0 z-10 pointer-events-none" 
      width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"
    >
      <g>
        <path d="M8 2.5H20" stroke="#0078D4" strokeWidth="1.2" strokeLinecap="square" />
        <path d="M8 0.5V4.5M20 0.5V4.5" stroke="#0078D4" strokeWidth="1.2" />
      </g>
      <g>
        <path d="M3.5 7V22" stroke="#0078D4" strokeWidth="1.2" strokeLinecap="square" />
        <path d="M1.5 7H5.5M1.5 22H5.5" stroke="#0078D4" strokeWidth="1.2" />
      </g>
    </svg>
  );

  return (
    <div className="flex flex-col flex-1 h-full overflow-hidden bg-[#F9F5EB]">
      <div className="flex flex-1 overflow-hidden pt-4">
        
        {/* ЛІВА ПАНЕЛЬ (Структура проекту) */}
        <aside className="w-64 bg-white rounded-r-[32px] border border-[#E8E2D2]/50 flex flex-col shrink-0 mb-4 shadow-sm">
          <div className="p-6 text-left">
            <h3 className="font-serif text-xl font-bold mb-6 text-[#433D33]">Structure</h3>
            <div className="flex items-center gap-2 py-2 font-bold text-sm text-[#4A0E0E]">
              <KeyboardArrowDown sx={{ fontSize: 18 }} />
              <FolderOpen sx={{ fontSize: 18 }} />
              <span>{book.title}</span>
            </div>
            <div className="ml-6 space-y-1">
               <div className="flex items-center gap-3 py-2 px-3 bg-[#F3EAD3] rounded-xl text-[#410D0D] font-bold text-xs shadow-sm cursor-pointer">
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
          {/* TOOLBAR (Панель інструментів) */}
          <div className="sticky top-4 bg-white/95 backdrop-blur-md px-4 py-2 rounded-2xl shadow-xl border border-[#E8E2D2]/50 flex items-center gap-1 mb-10 z-50 transition-all">
            
            {/* СЕЛЕКТОР ШРИФТІВ */}
            <div className="relative" ref={fontDropdownRef}>
              <button 
                onClick={() => {
                  setShowFontDropdown(!showFontDropdown);
                  setShowSizeDropdown(false);
                  setShowFormatDropdown(false);
                }}
                className={`flex items-center gap-2 px-3 py-1.5 rounded-xl transition-all border border-transparent w-[140px] justify-between ${
                  showFontDropdown ? 'bg-[#F3EAD3] border-[#E8E2D2]' : 'hover:bg-[#F3EAD3] hover:border-[#E8E2D2]'
                }`}
              >
                <span className="text-[13px] font-bold text-[#4A0E0E] truncate flex-1 text-left">
                  {editor?.getAttributes('textStyle').fontFamily?.split(',')[0].replace(/"/g, '') || 'Inter'}
                </span>
                <KeyboardArrowDown sx={{ fontSize: 18, color: '#4A0E0E' }} className="shrink-0"/>
              </button>

              {showFontDropdown && (
                <div className="absolute top-full left-0 mt-2 w-64 bg-white rounded-2xl shadow-2xl border border-[#E8E2D2] overflow-hidden z-[120] animate-in fade-in zoom-in duration-200">
                  <div className="p-1.5 flex flex-col gap-1 max-h-[350px] overflow-y-auto scrollbar-hide">
                    {FONTS.map((font) => (
                      <button 
                        key={font.name}
                        onClick={() => handleFontChange(font.family)}
                        className={`px-4 py-2 text-left rounded-xl transition-all flex items-center justify-between group ${
                          editor?.getAttributes('textStyle').fontFamily === font.family ? 'bg-[#E29700]/20' : 'hover:bg-[#F3EAD3]'
                        }`}
                      >
                        <div className="flex flex-col">
                          <span style={{ fontFamily: font.family }} className="text-[16px] text-[#4A0E0E]">
                            {font.name}
                          </span>
                          <span className="text-[9px] text-gray-400 uppercase font-bold tracking-wider leading-none mt-1">
                            {font.type}
                          </span>
                        </div>
                        {editor?.getAttributes('textStyle').fontFamily === font.family && (
                          <CheckCircleOutlined sx={{ fontSize: 16, color: '#E29700' }} />
                        )}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>

            <div className="w-px h-6 bg-gray-200 mx-1" />

            <div className="relative" ref={dropdownRef}>
              <div className="flex items-center gap-1 bg-[#F3EAD3]/30 hover:bg-[#F3EAD3]/60 rounded-xl px-2 py-1.5 transition-all border border-transparent hover:border-[#E8E2D2] mr-2">
                <input 
                  type="text"
                  value={fontSizeInput}
                  onChange={(e) => setFontSizeInput(e.target.value)}
                  onKeyDown={handleFontSizeKeyDown}
                  onFocus={() => setShowSizeDropdown(true)}
                  className="w-8 bg-transparent text-[14px] font-bold text-[#4A0E0E] outline-none text-center"
                />
                <KeyboardArrowDown 
                  className={`text-[#4A0E0E] cursor-pointer transition-transform ${showSizeDropdown ? 'rotate-180' : ''}`} 
                  sx={{ fontSize: 18 }}
                  onClick={() => setShowSizeDropdown(!showSizeDropdown)}
                />
              </div>

              {showSizeDropdown && (
              <div className="absolute top-full right-0 mt-2 w-16 bg-white rounded-2xl shadow-2xl border border-[#E8E2D2] overflow-hidden z-[100] animate-in fade-in zoom-in duration-200">
                <div className="max-h-60 overflow-y-auto p-2 scrollbar-hide flex flex-col gap-1">
                  {FONT_SIZES.map(size => (
                    <div 
                      key={size}
                      onClick={() => handleFontSizeChange(size)}
                      className={`px-2 py-2 text-center text-[12px] font-bold cursor-pointer transition-all rounded-xl ${
                        fontSizeInput === size 
                          ? 'bg-[#E29700]/20 text-black' 
                          : 'text-[#4A0E0E] hover:bg-[#E29700]/10'
                      }`}
                    >
                      {size}
                    </div>
                  ))}
                </div>
              </div>
            )}
            </div>

            <div className="w-px h-6 bg-gray-200 mx-1" />

            {/* Стилі тексту */}
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

            {/* Вирівнювання */}
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

            {/* Списки */}
            <ToolbarButton 
              active={editor.isActive('bulletList')} 
              onClick={() => editor.chain().focus().toggleBulletList().run()} 
              IconComponent={FormatListBulleted} 
            />
            <ToolbarButton 
              active={editor.isActive('orderedList')} 
              onClick={() => editor.chain().focus().toggleOrderedList().run()} 
              IconComponent={FormatListNumbered} 
            />
            
            <div className="w-px h-6 bg-gray-200 mx-2" />

            <button 
              onMouseDown={(e) => { 
                e.preventDefault(); 
                // @ts-ignore
                editor.chain().focus().setPageBreak().run(); 
              }}
              className="p-2 rounded-lg transition-all text-gray-500 hover:bg-[#F3EAD3] hover:text-[#4A0E0E]"
              title="Розрив сторінки"
            >
              <HorizontalRule sx={{ fontSize: 20 }} />
            </button>

            <div className="w-px h-6 bg-gray-200 mx-2" />

            {/* Формат паперу */}
            
            <div className="relative" ref={formatDropdownRef}>

              <button 
                onClick={() => {
                  setShowFormatDropdown(!showFormatDropdown);
                  setShowSizeDropdown(false);
                }}
                className={`flex flex-col items-center justify-center gap-1 px-3 py-2 rounded-xl transition-all border border-transparent ${
                  showFormatDropdown ? 'bg-[#F3EAD3] border-[#E8E2D2]' : 'hover:bg-[#F3EAD3] hover:border-[#E8E2D2]'
                }`}
              >
                {/* КОМПОЗИЦІЯ ІКОНОК */}
                <div className="relative w-6 h-6 flex items-center justify-center">
                  {/* 1. Наші кастомні сині лінійки (SVG) */}
                  <WordSizeIcon />

                  {/* 2. Стандартна іконка Material 3 */}
                  {/* Ми додаємо невеликий margin (ml-1 mt-1), щоб "відсунути" її від лінійок */}
                  <InsertDriveFile 
                    className="ml-1.5 mt-1.5 opacity-90"
                    sx={{ fontSize: 20,}} 
                  />
                </div>
                
              </button>

              {showFormatDropdown && (
                <div className="absolute top-full right-0 mt-2 w-48 bg-white rounded-2xl shadow-2xl border border-[#E8E2D2] overflow-hidden z-[110] animate-in fade-in zoom-in duration-200">
                  <div className="p-2 flex flex-col gap-1 max-h-[400px] overflow-y-auto scrollbar-hide">
                    {Object.entries(PAGE_FORMATS).map(([key, format]) => (
                      <div 
                        key={key}
                        onClick={() => {
                          setCurrentFormat(key as FormatKey);
                          setShowFormatDropdown(false);
                          setShowFontDropdown(false);
                        }}
                        className={`flex items-center gap-4 px-2 py-1.5 cursor-pointer transition-all rounded-xl ${
                          currentFormat === key ? 'bg-[#E29700]/20' : 'hover:bg-[#F3EAD3]'
                        }`}
                      >
                        {/* Міні-іконка пропорції (додано фон для видимості) */}
                        <div className="w-10 flex justify-center items-center h-12 bg-gray-50 rounded-lg border border-gray-100">
                          <div 
                            className="border-[1.5px] border-[#4A0E0E]/40 bg-white shadow-sm"
                            style={{ 
                              width: Math.min(24, (format.width / format.height) * 32) + 'px', 
                              height: '32px' 
                            }}
                          />
                        </div>
                        
                        <div className="flex flex-col text-left">
                          <span className="text-[14px] font-bold text-[#4A0E0E]">
                            {format.name}
                          </span>
                          <span className="text-[11px] text-gray-400 font-medium italic">
                            {format.sub}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

          </div>

          {/* КОНТЕЙНЕР ДЛЯ ВЕРТИКАЛЬНИХ СТОРІНОК */}
          <div 
            className="transition-transform duration-100 origin-top pb-40 flex flex-col items-center gap-10"
            style={{ transform: `scale(${zoom / 100})` }}
          >
            <div className="relative shadow-2xl border border-gray-200 bg-white flex transition-all duration-300 ease-in-out"
                 style={{ 
                   width: `${PAGE_FORMATS[currentFormat].width}px`, 
                   minHeight: `${PAGE_FORMATS[currentFormat].height}px` 
                 }}>
              
              {/* ЛІВА ЛІНІЙКА СТОРІНОК */}
              <div className="w-16 shrink-0 border-r border-[#E8E2D2] bg-[#fdfcf9] relative overflow-hidden pointer-events-none">
                {Array.from({ length: pageCount }).map((_, i) => (
                  <div 
                    key={i} 
                    className="absolute w-full flex flex-col items-center"
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

              {/* ЗОНА ТЕКСТУ РЕДАКТОРА */}
              <div 
                className="flex-1" 
                style={{ 
                  padding: currentFormat === 'Pocket' ? '40px 30px' : '40px 54px', 
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
                    wordBreak: 'break-word', 
                    overflowWrap: 'anywhere'
                  }}
                />
              </div>
            </div>
          </div>
        </main>

        {/* ПРАВА ПАНЕЛЬ (Нотатки) */}
        <aside className="w-80 bg-white rounded-l-[32px] border border-[#E8E2D2]/50 flex flex-col shrink-0 mb-4 shadow-sm overflow-hidden">
          <div className="p-6">
            <h4 className="font-bold text-xs mb-4 text-gray-400 uppercase tracking-widest">Notes</h4>
            <div className="bg-green-50 p-4 rounded-2xl border border-green-100 mb-4 relative">
                <CheckCircleOutlined className="text-green-500 absolute top-4 right-4" sx={{ fontSize: 18 }} />
                <span className="text-[10px] font-bold text-green-600 block mb-1 uppercase">Comment</span>
                <p className="text-xs text-green-800 leading-relaxed pr-6">Ця панель допоможе вам у роботі над LiberInk.</p>
            </div>
            <button className="w-full py-4 border-2 border-dashed border-gray-100 rounded-2xl text-gray-300 hover:text-orange-400 transition-all">
              <Add />
            </button>
          </div>
        </aside>
      </div>

      {/* FOOTER (Індикатори стану) */}
      <footer className="h-12 bg-[#F9F5EB] border-t border-[#E8E2D2] flex items-center justify-between px-10 shrink-0 z-20 font-sans text-[11px] font-bold text-[#433D33]">
        <div className="flex gap-10">
           <span>PAGES: {pageCount}</span>
           <span>WORDS: {wordCount}</span>
        </div>
        <div className="flex items-center gap-4 w-64">
           <input 
              type="range" min="30" max="200" value={zoom} 
              onChange={(e) => setZoom(Number(e.target.value))} 
              className="flex-1 h-1 bg-gray-300 appearance-none accent-[#4A0E0E] cursor-pointer" 
           />
           <span className="min-w-[35px] text-right">{Math.round(zoom)}%</span>
        </div>
      </footer>
    </div>
  );
};

// ДОПОМІЖНИЙ КОМПОНЕНТ КНОПКИ
const ToolbarButton = ({ active, onClick, IconComponent }: { active: boolean, onClick: () => void, IconComponent: React.ElementType }) => (
  <button 
    onMouseDown={(e) => { e.preventDefault(); onClick(); }}
    className={`p-2 rounded-lg transition-all ${
      active ? 'bg-[#4A0E0E] text-white shadow-md scale-105' : 'text-gray-500 hover:bg-[#F3EAD3] hover:text-[#4A0E0E]'
    }`}
  >
    <IconComponent sx={{ fontSize: 20 }} />
  </button>
);

export default EditorPage;