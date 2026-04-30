import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import TextAlign from '@tiptap/extension-text-align';
import { TextStyle } from '@tiptap/extension-text-style';
import { FontSize } from './FontSize'; 
import { PageBreak } from './PageBreak.tsx';
import FontFamily from '@tiptap/extension-font-family';
import Color from '@tiptap/extension-color';

// DND Kit Imports
import { 
  DndContext, closestCenter, KeyboardSensor, PointerSensor, 
  useSensor, useSensors, DragOverlay, useDraggable, useDroppable,
  defaultKeyboardCoordinateGetter, // Додай це
  type DragEndEvent 
} from '@dnd-kit/core';


// Імпорти іконок Material UI
import {
  FormatBold, FormatItalic, FormatUnderlined, StrikethroughS,
  FormatAlignLeft, FormatAlignCenter, FormatAlignRight, FormatAlignJustify,
  FormatListBulleted, FormatListNumbered, Add, CheckCircleOutlined,
  FolderOpen, KeyboardArrowDown, InsertDriveFile, HorizontalRule,
  DragIndicator, ChevronLeft, MenuOpen
} from '@mui/icons-material';

// --- 1. ІНТЕРФЕЙСИ ТА КОНСТАНТИ ---
interface Book {
  id: number;
  title: string;
  description: string;
  userId: string;
  authorPseudonym: string;
  lastEdited: string;
}

interface BookElement {
  id: number;
  title: string;
  subtitle?: string;
  type: 'Part' | 'Chapter';
  children: BookElement[];
}

const FONTS = [
  { name: 'Garamond', family: '"EB Garamond", serif', type: 'Книжкова класика' },
  { name: 'Merriweather', family: 'Merriweather, serif', type: 'Комфортне читання' },
  { name: 'Lora', family: 'Lora, serif', type: 'Сучасна проза' },
  { name: 'Courier Prime', family: '"Courier Prime", monospace', type: 'Друкарська машинка' },
  { name: 'Inter', family: 'Inter, sans-serif', type: 'Сучасний стиль' },
];

const ACCENT_COLORS = [
  { name: 'Ink Black', color: '#333333' },
  { name: 'Blood Red', color: '#8B0000' },
  { name: 'Deep Forest', color: '#1B4D3E' },
  { name: 'Royal Blue', color: '#1A365D' },
  { name: 'Old Gold', color: '#B8860B' },
];

const PAGE_FORMATS = {
  A4: { width: 794, height: 1123, name: 'A4', sub: '21 cm x 29,7 cm' },
  A5: { width: 559, height: 794, name: 'A5', sub: '14,8 cm x 21 cm' },
  Letter: { width: 816, height: 1056, name: 'Letter', sub: '21,59 cm x 27,94 cm' },
  Pocket: { width: 416, height: 605, name: 'Pocket', sub: '11 cm x 15 cm' },
};

const FONT_SIZES = ['8', '10', '12', '14', '16', '18', '20', '24', '30', '36', '48', '60', '72'];
type FormatKey = keyof typeof PAGE_FORMATS;

// --- 2. ДОПОМІЖНІ КОМПОНЕНТИ ---
const StructureItem = ({ item, selectedId, onSelect, onAddSub, handleMoveToRoot, handleDelete, isChild }: any) => {
  const [isExpanded, setIsExpanded] = useState(true);

  // Налаштування для перетягування (Джерело)
  const { attributes, listeners, setNodeRef: setDraggableRef, isDragging } = useDraggable({
    id: item.id.toString(),
    data: { type: item.type, item }
  });

  // Налаштування для кидання (Ціль - тільки папки приймають файли)
  const { setNodeRef: setDroppableRef, isOver } = useDroppable({
    id: item.id.toString(),
    disabled: item.type !== 'Part', 
    data: { type: item.type }
  });

  // Об'єднуємо рефи для DND
  const setNodeRef = (node: HTMLElement | null) => {
    setDraggableRef(node);
    setDroppableRef(node);
  };

  const isSelected = selectedId === item.id;

  return (
    <div ref={setNodeRef} className={`flex flex-col group relative transition-all ${isDragging ? 'opacity-40 grayscale' : ''}`}>
      <div 
        onClick={() => {
          if (item.type === 'Part') setIsExpanded(!isExpanded);
          onSelect(item.id, item.type);
        }}
        className={`flex items-center gap-2 py-2 px-3 cursor-pointer rounded-xl mb-0.5 border-2 transition-all ${
          isSelected 
            ? 'bg-[#F3EAD3] border-[#4A0E0E]/20 text-[#410D0D] font-bold shadow-sm' 
            : isOver 
              ? 'bg-[#4A0E0E]/10 border-dashed border-[#4A0E0E]/40 scale-[1.02]' 
              : 'bg-transparent border-transparent hover:bg-[#F3EAD3]/40 text-[#433D33]'
        }`}
      >
        {/* Хендл для перетягування */}
        <div {...attributes} {...listeners} className="cursor-grab opacity-20 hover:opacity-100 p-1 flex items-center">
          <DragIndicator sx={{ fontSize: 16 }} />
        </div>

        {/* Іконка типу елемента */}
        {item.type === 'Part' ? (
          <KeyboardArrowDown sx={{ 
            fontSize: 18, 
            opacity: 0.7, 
            transform: isExpanded ? 'rotate(0deg)' : 'rotate(-90deg)', 
            transition: 'transform 0.2s ease' 
          }} />
        ) : (
          <InsertDriveFile sx={{ fontSize: 14, opacity: 0.4 }} />
        )}

        {/* Назва та кнопка витягування */}
        <div className="flex items-center gap-2 flex-1 overflow-hidden">
          <span className="text-[13px] truncate">{item.title}</span>
          
          {/* Кнопка з'являється тільки для вкладених елементів при наведенні */}
          {isChild && (
            <button 
              onClick={(e) => { 
                e.stopPropagation(); 
                handleMoveToRoot(item.id); 
              }}
              className="opacity-0 group-hover:opacity-100 p-0.5 hover:bg-[#4A0E0E]/10 rounded text-gray-400 hover:text-[#4A0E0E] transition-all"
              title="Витягнути в корінь"
            >
              <ChevronLeft sx={{ fontSize: 16 }} />
            </button>
          )}
        </div>

        {/* Кнопка додавання глави в папку */}
        {item.type === 'Part' && (
          <button 
            onClick={(e) => { e.stopPropagation(); onAddSub('Chapter', item.id); }} 
            className="opacity-0 group-hover:opacity-100 p-1 hover:bg-[#4A0E0E]/10 rounded-md transition-all"
          >
            <Add sx={{ fontSize: 14, color: '#4A0E0E' }} />
          </button>
        )}
      </div>

      {/* Рендер вкладених дітей */}
      {isExpanded && item.children && item.children.length > 0 && (
        <div className="flex flex-col border-l border-[#E8E2D2]/60 ml-4 pl-1 mt-1 transition-all">
          {item.children.map((child: any) => (
            <StructureItem
              key={child.id}
              item={child}
              selectedId={selectedId}
              onSelect={onSelect}
              onAddSub={onAddSub}
              handleMoveToRoot={handleMoveToRoot}
              handleDelete = {handleDelete}
              isChild={true}
            />
          ))}
        </div>
      )}
    </div>
  );
};

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


// --- 3. ГОЛОВНИЙ КОМПОНЕНТ РЕДАКТОРА ---
const EditorPage = () => {

  const { isOver: isOverRoot, setNodeRef: setRootDropRef } = useDroppable({ id: 'root-drop-zone' });
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  // --- УСІ ХУКИ (useState, useRef, useCallback, useEffect) НА САМОМУ ПОЧАТКУ ---
  
  // Стани книги та структури
  const [book, setBook] = useState<Book | null>(null);
  const [structure, setStructure] = useState<BookElement[]>([]);
  const [selectedElementId, setSelectedElementId] = useState<number | null>(null);
  const [isAdding, setIsAdding] = useState(false);

  const [isSidebarOpen, setIsSidebarOpen] = useState(true);
  const [activeItem, setActiveItem] = useState<BookElement | null>(null);

  // Стани редактора та UI
  const [zoom, setZoom] = useState(100);
  const [wordCount, setWordCount] = useState(0);
  const [pageCount, setPageCount] = useState(1);
  const [currentFormat, setCurrentFormat] = useState<FormatKey>('A4');
  const [fontSizeInput, setFontSizeInput] = useState('11');
  const [saveStatus, setSaveStatus] = useState<'saved' | 'saving' | 'error'>('saved');
  const [, setUpdateTick] = useState(0);

  // Стани дропдаунів
  const [showSizeDropdown, setShowSizeDropdown] = useState(false);
  const [showFontDropdown, setShowFontDropdown] = useState(false);
  const [showColorDropdown, setShowColorDropdown] = useState(false);
  const [showFormatDropdown, setShowFormatDropdown] = useState(false);

  // Рефи
  const dropdownRef = useRef<HTMLDivElement>(null);
  const fontDropdownRef = useRef<HTMLDivElement>(null);
  const colorDropdownRef = useRef<HTMLDivElement>(null);
  const formatDropdownRef = useRef<HTMLDivElement>(null);
  const editorContainerRef = useRef<HTMLDivElement>(null);
  const currentFormatRef = useRef<FormatKey>(currentFormat);

  // Сенсори для DndKit
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(KeyboardSensor, { coordinateGetter: defaultKeyboardCoordinateGetter })
  );

  // Функції
  const forceUpdate = useCallback(() => setUpdateTick(tick => tick + 1), []);

  const fetchStructure = useCallback(async () => {
    if (!id) return;
    try {
      const response = await fetch(`http://localhost:5241/api/Books/structure/${id}`);
      if (response.ok) {
        const data = await response.json();
        setStructure(data);
      }
    } catch (err) {
      console.error("Failed to fetch structure", err);
    }
  }, [id]);

  const saveContent = useCallback(async (content: string) => {
    if (!book || !id) return;
    setSaveStatus('saving');
    try {
      const url = selectedElementId 
        ? `http://localhost:5241/api/Books/elements/${selectedElementId}`
        : `http://localhost:5241/api/Books/${id}`;

      const bodyData = selectedElementId ? {
        id: selectedElementId,
        content: content,
        bookId: parseInt(id),
        type: 'Chapter',
        title: "Chapter" // Можна покращити, шукаючи реальну назву в structure
      } : { 
        ...book, 
        description: content,
        lastEdited: new Date().toISOString() 
      };

      const response = await fetch(url, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(bodyData),
      });

      if (!response.ok) throw new Error();
      setSaveStatus('saved');
    } catch (err) {
      console.error("Auto-save failed", err);
      setSaveStatus('error');
    }
  }, [id, book, selectedElementId]);

  // Ініціалізація TipTap
  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        bulletList: { keepMarks: true, keepAttributes: false },
        orderedList: { keepMarks: true, keepAttributes: false },
      }),
      TextStyle,
      Color,
      FontSize,
      FontFamily,
      TextAlign.configure({ 
        types: ['heading', 'paragraph', 'bulletList', 'orderedList'], 
      }),
      PageBreak, 
    ],
    editorProps: {
      attributes: {
        class: 'outline-none ProseMirror h-full min-h-[500px]',
      },
    },
    onUpdate: ({ editor }) => {
      const text = editor.getText();
      setWordCount(text.trim() ? text.trim().split(/\s+/).length : 0);

      const pageHeight = PAGE_FORMATS[currentFormatRef.current].height;
      const contentHeight = editor.view.dom.scrollHeight + 180; 
      const calculatedPages = Math.max(1, Math.ceil(contentHeight / pageHeight));
      if (calculatedPages !== pageCount) setPageCount(calculatedPages);

      const attrs = editor.getAttributes('textStyle');
      setFontSizeInput(attrs.fontSize ? attrs.fontSize.replace('pt', '') : '14');

      setSaveStatus('saving');
      const timer = setTimeout(() => {
        saveContent(editor.getHTML());
      }, 2000);

      forceUpdate();
      return () => clearTimeout(timer);
    },
    onSelectionUpdate: forceUpdate,
    onTransaction: forceUpdate,
  });

  // Ефекти (useEffect)
  useEffect(() => {
    currentFormatRef.current = currentFormat;
  }, [currentFormat]);

  useEffect(() => {
    fetchStructure();
  }, [fetchStructure]);

  useEffect(() => {
    const fetchBook = async () => {
      try {
        const response = await fetch(`http://localhost:5241/api/Books/${id}`);
        if (!response.ok) throw new Error("Книгу не знайдено");
        const data = await response.json();
        setBook(data);
        // Завантажуємо опис книги ТІЛЬКИ якщо не вибрана жодна глава
        if (editor && data.description && !selectedElementId) {
          editor.commands.setContent(data.description);
        }
      } catch (err) {
        navigate('/library');
      }
    };
    fetchBook();
  }, [id, editor, navigate, selectedElementId]);

  useEffect(() => {
    const handleGlobalSave = () => {
      if (editor) saveContent(editor.getHTML());
    };
    window.addEventListener('trigger-save', handleGlobalSave);
    return () => window.removeEventListener('trigger-save', handleGlobalSave);
  }, [editor, saveContent]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as Node;
      if (dropdownRef.current && !dropdownRef.current.contains(target)) setShowSizeDropdown(false);
      if (fontDropdownRef.current && !fontDropdownRef.current.contains(target)) setShowFontDropdown(false);
      if (formatDropdownRef.current && !formatDropdownRef.current.contains(target)) setShowFormatDropdown(false);
      if (colorDropdownRef.current && !colorDropdownRef.current.contains(target)) setShowColorDropdown(false);
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    const handleWheel = (e: WheelEvent) => {
      if (e.ctrlKey || e.metaKey) {
        e.preventDefault();
        setZoom((prev) => Math.min(Math.max(prev - e.deltaY * 0.4, 30), 200));
      }
    };
    window.addEventListener('wheel', handleWheel, { passive: false });
    return () => window.removeEventListener('wheel', handleWheel);
  }, []);

  // --- 4. ПЕРЕВІРКА НА ЗАВАНТАЖЕННЯ (ПІСЛЯ ВСІХ ХУКІВ!) ---
  if (!editor || !book) {
    return (
      <div className="flex flex-col flex-1 h-screen w-full items-center justify-center bg-[#F9F5EB]">
        <div className="font-serif text-[#4A0E0E] text-xl animate-pulse">Loading LiberInk...</div>
      </div>
    );
  }

  // --- 5. ОБРОБНИКИ ПОДІЙ (HANDLERS) ---
  const handleElementClick = async (elementId: number, type: string) => {
    if (type === 'Part') return; 
    setSelectedElementId(elementId);
    try {
      const response = await fetch(`http://localhost:5241/api/Books/elements/${elementId}`);
      if (response.ok) {
        const data = await response.json();
        editor?.commands.setContent(data.content || '<p></p>');
      }
    } catch (err) {
      console.error("Помилка завантаження глави:", err);
    }
  };

  const handleAddElement = async (type: 'Part' | 'Chapter', parentId: number | null = null) => {
    if (isAdding) return;

    // 1. ВІЗУАЛ: Запитуємо назву у користувача
    const title = window.prompt(`Введіть назву для ${type === 'Part' ? 'нової частини' : 'нової глави'}:`);
    
    // Якщо користувач натиснув "Скасувати" або ввів пусту строку — зупиняємо
    if (!title || title.trim() === '') return;

    setIsAdding(true);

    const newElement = {
      title: title,
      subtitle: type === 'Chapter' ? 'Draft' : null,
      type: type,
      bookId: parseInt(id!), 
      parentId: parentId,
      order: structure.length + 1, 
      content: type === 'Chapter' ? '<p></p>' : null 
    };

    try {
      const response = await fetch('http://localhost:5241/api/Books/elements', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newElement),
      });

      if (response.ok) {
        await fetchStructure();
      } else {
        const errorText = await response.text();
        console.error("Бекенд відхилив запит (400):", errorText);
        alert("Помилка на сервері! Відкрийте консоль (F12) для деталей.");
      }
    } catch (err) {
      console.error("Помилка мережі при додаванні:", err);
    } finally {
      setIsAdding(false);
    }
  };
  
  const handleDragStart = (event: any) => {
    setActiveItem(event.active.data.current.item);
  };

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveItem(null);

    if (!over || active.id === over.id) return;

    const draggedId = active.id;
    const overId = over.id;

    // Якщо кинули на папку, вона стає новим батьком. 
    // Якщо кинули в порожню зону внизу (root-drop-zone), батько стає null.
    const newParentId = overId === 'root-drop-zone' ? null : parseInt(overId.toString());

    try {
      await fetch(`http://localhost:5241/api/Books/elements/${draggedId}/move`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ newParentId, newOrder: 0 }),
      });
      fetchStructure();
    } catch (err) { console.error(err); }
  };

  const handleMoveToRoot = async (elementId: number) => {
    try {
      await fetch(`http://localhost:5241/api/Books/elements/${elementId}/move`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ newParentId: null, newOrder: 0 }),
      });
      fetchStructure();
    } catch (err) {
      console.error(err);
    }
  };

  const handleDeleteElement = async (elementId: number, title: string) => {
  if (!window.confirm(`Ви впевнені, що хочете видалити "${title}"?`)) return;

  try {
    const response = await fetch(`http://localhost:5241/api/Books/elements/${elementId}`, {
      method: 'DELETE',
    });

    if (response.ok) {
      // Якщо ми видалили ту главу, яка зараз відкрита — очищуємо редактор
      if (selectedElementId === elementId) {
        setSelectedElementId(null);
        editor?.commands.setContent('<p></p>');
      }
      fetchStructure();
    }
  } catch (err) {
    console.error("Помилка при видаленні:", err);
  }
};

  const handleFontChange = (family: string) => {
    editor?.chain().focus().setFontFamily(family).run();
    setShowFontDropdown(false);
  };

  const handleFontSizeChange = (value: string) => {
    setFontSizeInput(value);
    const size = parseInt(value);
    if (!isNaN(size) && size > 0 && size < 200) {
      editor?.chain().focus().setFontSize(`${size}pt`).run();
    }
    setShowSizeDropdown(false);
  };

  const handleFontSizeKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleFontSizeChange(fontSizeInput);
      editor?.commands.focus();
    }
  };

  // Flat array of IDs needed for DndKit root level context
  // --- 6. JSX РЕНДЕР ---
  return (
    <div className="flex flex-col flex-1 h-full overflow-hidden bg-[#F9F5EB]">
      <div className="flex flex-1 overflow-hidden pt-0">

        {!isSidebarOpen && (
        <button 
          onClick={() => setIsSidebarOpen(true)}
          className="absolute left-4 top-6 z-[60] bg-[#FFFCF5] p-2 rounded-full shadow-lg border border-[#E8E2D2] text-[#4A0E0E] hover:bg-[#F3EAD3] transition-all"
        >
          <MenuOpen sx={{ fontSize: 24 }} />
        </button>
      )}
        
        {/* ЛІВА ПАНЕЛЬ (Структура проекту) */}

        <aside 
          className="bg-[#FFFCF5] rounded-r-[32px] border-r border-[#E8E2D2]/50 flex flex-col shrink-0 mb-0 shadow-sm overflow-hidden h-full transition-all duration-500 ease-in-out z-50"
          style={{ 
            width: isSidebarOpen ? '240px' : '0px', 
            opacity: isSidebarOpen ? 1 : 0,
            pointerEvents: isSidebarOpen ? 'auto' : 'none'
          }}
        >
          <div className="p-5 flex flex-col h-full w-[240px]">
            {/* Заголовок панелі */}
            <div className="flex items-center justify-between mb-6 px-1">
              <h3 className="font-serif text-xl font-bold text-[#433D33]">Book Map</h3>
              <button 
                onClick={() => setIsSidebarOpen(false)}
                className="p-1 hover:bg-[#F3EAD3] rounded-lg text-gray-400 hover:text-[#4A0E0E] transition-all"
              >
                <ChevronLeft />
              </button>
              <div className="flex gap-1">
                <button 
                  onClick={() => handleAddElement('Part')}
                  className="p-1.5 hover:bg-[#F3EAD3] rounded-xl transition-all text-[#4A0E0E]"
                  title="Додати частину"
                >
                  <FolderOpen sx={{ fontSize: 20 }} />
                </button>
                <button 
                  onClick={() => handleAddElement('Chapter')}
                  className="p-1.5 hover:bg-[#F3EAD3] rounded-xl transition-all text-[#4A0E0E]"
                  title="Додати главу"
                >
                  <Add sx={{ fontSize: 22 }} />
                </button>


              </div>
            </div>

            {/* Основна область списку з DND */}
            <div className="flex-1 overflow-y-auto pr-1 scrollbar-hide">
              {structure.length > 0 ? (
                <DndContext 
                  sensors={sensors} 
                  collisionDetection={closestCenter} 
                  onDragStart={handleDragStart} 
                  onDragEnd={handleDragEnd}
                >
                  {/* Глобальна зона для перетягування (Скидання в корінь) */}
                  <div 
                    ref={setRootDropRef} 
                    className={`space-y-1 min-h-[400px] pb-40 transition-all rounded-2xl p-1 ${
                      isOverRoot ? 'bg-[#4A0E0E]/5 outline-2 outline-dashed outline-[#4A0E0E]/20' : ''
                    }`}
                  >
                    {structure.map((item) => (
                      <StructureItem 
                        key={item.id} 
                        item={item} 
                        selectedId={selectedElementId} 
                        onSelect={handleElementClick} 
                        onAddSub={handleAddElement}
                        handleMoveToRoot={handleMoveToRoot}
                        handleDelete={handleDeleteElement}
                        isChild={false} // Кореневі елементи
                      />
                    ))}
                    
                    {isOverRoot && (
                      <div className="pointer-events-none py-10 text-center">
                        <span className="text-[10px] text-[#4A0E0E]/40 uppercase font-bold tracking-widest">
                          Release to move to root
                        </span>
                      </div>
                    )}
                  </div>

                  {/* Привид елемента під курсором */}
                  <DragOverlay dropAnimation={null}>
                    {activeItem ? (
                      <div className="flex items-center gap-2 py-2 px-4 bg-[#FFFCF5] border-2 border-[#4A0E0E]/20 text-[#410D0D] rounded-xl shadow-2xl opacity-95 scale-[1.05] cursor-grabbing min-w-[200px] z-[1000]">
                        {activeItem.type === 'Part' ? 
                          <FolderOpen sx={{ fontSize: 18, color: '#E29700' }} /> : 
                          <InsertDriveFile sx={{ fontSize: 16, opacity: 0.6 }} 
                        />}
                        <span className="text-[13px] font-bold truncate tracking-wide">
                          {activeItem.title}
                        </span>
                      </div>
                    ) : null}
                  </DragOverlay>
                </DndContext>
              ) : (
                <div className="flex flex-col items-center justify-center h-40 opacity-30 text-center">
                  <InsertDriveFile sx={{ fontSize: 32, mb: 1 }} />
                  <p className="text-[11px] font-bold uppercase tracking-widest">No elements yet</p>
                </div>
              )}
            </div>

            {/* Картка автора внизу */}
            <div className="mt-auto pt-4 border-t border-[#E8E2D2]/30">
              <div className="flex items-center justify-between px-2 py-3 bg-[#F3EAD3]/30 rounded-2xl cursor-pointer hover:bg-[#F3EAD3]/50 transition-all group">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-full bg-[#4A0E0E] flex items-center justify-center text-white text-xs shadow-sm">
                    {book.authorPseudonym.charAt(0)}
                  </div>
                  <div className="flex flex-col">
                    <span className="text-[11px] font-bold text-[#433D33]">{book.authorPseudonym}</span>
                    <span className="text-[9px] text-gray-400 uppercase tracking-tighter">Author</span>
                  </div>
                </div>
                <KeyboardArrowDown sx={{ fontSize: 16, opacity: 0.3 }} className="group-hover:translate-y-0.5 transition-transform" />
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
          <div className="sticky top-1 bg-[#FFFCF5] backdrop-blur-md px-4 py-2 rounded-2xl shadow-xl border border-[#E8E2D2]/50 flex items-center gap-1 mb-10 z-50 transition-all">
            
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
              <div className="flex items-center gap-1 hover:bg-[#F3EAD3]/60 rounded-xl px-2 py-1.5 transition-all border border-transparent hover:border-[#E8E2D2] mr-2">
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

            <ToolbarButton active={editor.isActive('bold')} onClick={() => editor.chain().focus().toggleBold().run()} IconComponent={FormatBold} />
            <ToolbarButton active={editor.isActive('italic')} onClick={() => editor.chain().focus().toggleItalic().run()} IconComponent={FormatItalic} />
            <ToolbarButton active={editor.isActive('underline')} onClick={() => editor.chain().focus().toggleUnderline().run()} IconComponent={FormatUnderlined} />
            <ToolbarButton active={editor.isActive('strike')} onClick={() => editor.chain().focus().toggleStrike().run()} IconComponent={StrikethroughS} />

            <div className="relative" ref={colorDropdownRef}>
              <div className="flex items-center rounded-xl border border-transparent hover:border-[#E8E2D2] transition-all">
                <button 
                  onClick={() => {
                    const currentColor = editor.getAttributes('textStyle').color || ACCENT_COLORS[0].color;
                    editor.chain().focus().setColor(currentColor).run();
                  }}
                  className="p-2 hover:bg-[#F3EAD3] rounded-l-xl transition-all"
                  title="Застосувати колір"
                >
                  <div 
                    className="w-4 h-4 rounded-full border border-black/10 shadow-sm" 
                    style={{ backgroundColor: editor.getAttributes('textStyle').color || '#333' }}
                  />
                </button>
                <button 
                  onClick={() => {
                    setShowColorDropdown(!showColorDropdown);
                    setShowFontDropdown(false);
                    setShowSizeDropdown(false);
                  }}
                  className="p-2 pl-0 hover:bg-[#F3EAD3] rounded-r-xl transition-all"
                >
                  <KeyboardArrowDown sx={{ fontSize: 16, color: '#4A0E0E' }} className={`transition-transform ${showColorDropdown ? 'rotate-180' : ''}`} />
                </button>
              </div>

              {showColorDropdown && (
                <div className="absolute top-full left-0 mt-2 w-40 bg-white rounded-2xl shadow-2xl border border-[#E8E2D2] p-2 z-[130] animate-in fade-in zoom-in duration-200">
                  <div className="flex flex-col gap-1">
                    {ACCENT_COLORS.map((c) => (
                      <button
                        key={c.color}
                        onClick={() => {
                          editor.chain().focus().setColor(c.color).run();
                          setShowColorDropdown(false);
                        }}
                        className="flex items-center gap-3 px-3 py-2 hover:bg-[#F3EAD3] rounded-xl transition-all group"
                      >
                        <div className="w-5 h-5 rounded-full shrink-0 shadow-inner border border-black/5" style={{ backgroundColor: c.color }} />
                        <span className="text-[12px] font-bold text-[#433D33] group-hover:text-[#4A0E0E]">{c.name}</span>
                      </button>
                    ))}
                    <div className="h-px bg-gray-100 my-1" />
                    <button
                      onClick={() => {
                        editor.chain().focus().unsetColor().run();
                        setShowColorDropdown(false);
                      }}
                      className="w-full py-2 text-[11px] font-bold text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all"
                    >
                      Reset Color
                    </button>
                  </div>
                </div>
              )}
            </div>

            <div className="w-px h-6 bg-gray-200 mx-2" />

            <ToolbarButton active={editor.isActive({ textAlign: 'left' })} onClick={() => editor.chain().focus().setTextAlign('left').run()} IconComponent={FormatAlignLeft} />
            <ToolbarButton active={editor.isActive({ textAlign: 'center' })} onClick={() => editor.chain().focus().setTextAlign('center').run()} IconComponent={FormatAlignCenter} />
            <ToolbarButton active={editor.isActive({ textAlign: 'right' })} onClick={() => editor.chain().focus().setTextAlign('right').run()} IconComponent={FormatAlignRight} />
            <ToolbarButton active={editor.isActive({ textAlign: 'justify' })} onClick={() => editor.chain().focus().setTextAlign('justify').run()} IconComponent={FormatAlignJustify} />
            
            <div className="w-px h-6 bg-gray-200 mx-2" />

            <ToolbarButton active={editor.isActive('bulletList')} onClick={() => editor.chain().focus().toggleBulletList().run()} IconComponent={FormatListBulleted} />
            <ToolbarButton active={editor.isActive('orderedList')} onClick={() => editor.chain().focus().toggleOrderedList().run()} IconComponent={FormatListNumbered} />
            
            <div className="w-px h-6 bg-gray-200 mx-2" />

            <button 
              onMouseDown={(e) => { 
                e.preventDefault(); 
                (editor.chain().focus() as any).setPageBreak().run(); 
              }}
              className="p-2 rounded-lg transition-all text-gray-500 hover:bg-[#F3EAD3] hover:text-[#4A0E0E]"
              title="Розрив сторінки"
            >
              <HorizontalRule sx={{ fontSize: 20 }} />
            </button>

            <div className="w-px h-6 bg-gray-200 mx-2" />

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
                <div className="relative w-6 h-6 flex items-center justify-center">
                  <WordSizeIcon />
                  <InsertDriveFile className="ml-1.5 mt-1.5 opacity-90" sx={{ fontSize: 20 }} />
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
                        <div className="w-10 flex justify-center items-center h-12 bg-gray-50 rounded-lg border border-gray-100">
                          <div 
                            className="border-[1.5px] border-[#4A0E0E]/40 bg-white shadow-sm"
                            style={{ width: Math.min(24, (format.width / format.height) * 32) + 'px', height: '32px' }}
                          />
                        </div>
                        <div className="flex flex-col text-left">
                          <span className="text-[14px] font-bold text-[#4A0E0E]">{format.name}</span>
                          <span className="text-[11px] text-gray-400 font-medium italic">{format.sub}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

          </div>

          <div 
            className="transition-transform duration-100 origin-top pb-40 flex flex-col items-center gap-10"
            style={{ transform: `scale(${zoom / 100})` }}
          >
            <div className="relative shadow-2xl border border-gray-200 bg-white flex transition-all duration-300 ease-in-out"
                 style={{ width: `${PAGE_FORMATS[currentFormat].width}px`, minHeight: `${PAGE_FORMATS[currentFormat].height}px` }}>
              
              <div className="w-16 shrink-0 border-r border-[#E8E2D2] bg-[#fdfcf9] relative overflow-hidden pointer-events-none">
                {Array.from({ length: pageCount }).map((_, i) => (
                  <div 
                    key={i} 
                    className="absolute w-full flex flex-col items-center"
                    style={{ top: `${i * PAGE_FORMATS[currentFormat].height}px` }} 
                  >
                    <span className="mt-4 text-[10px] font-bold text-gray-400">Стор. {i + 1}</span>
                    {i > 0 && <div className="absolute top-0 w-full border-t-2 border-dashed border-gray-300"></div>}
                  </div>
                ))}
              </div>

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
                  style={{ boxSizing: 'border-box', wordBreak: 'break-word', overflowWrap: 'anywhere' }}
                />
              </div>
            </div>
          </div>
        </main>

        {/* ПРАВА ПАНЕЛЬ */}
        <aside className="w-60 bg-[#FFFCF5] rounded-l-[24px] border border-[#E8E2D2]/50 flex flex-col shrink-0 mb-0 shadow-sm overflow-hidden">
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

      {/* ФУТЕР */}
      <footer className="h-8 bg-[#F9F5EB] border-t border-[#E8E2D2] flex items-center justify-between px-10 shrink-0 z-20 font-sans text-[11px] font-bold text-[#433D33]">
        <div className="flex gap-10">
           <span>PAGES: {pageCount}</span>
           <span>WORDS: {wordCount}</span>
        </div>

        <div className="flex items-center gap-2 text-[10px] uppercase tracking-widest transition-all">
          {saveStatus === 'saving' && (
            <span className="text-orange-500 animate-pulse flex items-center gap-1">
               <div className="w-1.5 h-1.5 bg-orange-500 rounded-full" /> Saving...
            </span>
          )}
          {saveStatus === 'saved' && (
            <span className="text-green-600 flex items-center gap-1">
              <CheckCircleOutlined sx={{ fontSize: 14 }} /> Saved
            </span>
          )}
          {saveStatus === 'error' && (
            <span className="text-red-500 font-bold cursor-pointer" onClick={() => saveContent(editor!.getHTML())}>
              Sync Error! Retry?
            </span>
          )}
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

export default EditorPage;