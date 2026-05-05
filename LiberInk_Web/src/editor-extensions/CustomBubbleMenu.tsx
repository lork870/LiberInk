import React, { useState, useEffect } from 'react';
import { Editor } from '@tiptap/react';

const CustomBubbleMenu = ({ editor }: { editor: Editor | null }) => {
  const [isVisible, setIsVisible] = useState(false);
  const [position, setPosition] = useState({ x: 0, y: 0 });

  useEffect(() => {
    if (!editor) return;

    const handleSelection = () => {
      const { empty, from, to } = editor.state.selection;
      
      // Якщо виділення немає, приховуємо меню
      if (empty || from === to) {
        setIsVisible(false);
        return;
      }

      try {
        const node = editor.view.domAtPos(from).node;
        if (node && node instanceof Element) {
          const rect = node.getBoundingClientRect();
          setPosition({
            x: rect.left + rect.width / 2,
            y: rect.top - 45 + window.scrollY,
          });
          setIsVisible(true);
        }
      } catch (err) {
        // Ігноруємо можливі помилки визначення позиції
      }
    };

    editor.on('selectionUpdate', handleSelection);
    editor.on('blur', () => setIsVisible(false));

    return () => {
      editor.off('selectionUpdate', handleSelection);
    };
  }, [editor]);

  if (!isVisible || !editor) return null;

  return (
    <div
      className="fixed z-[1000] bg-[#FFFCF5] border border-[#E8E2D2] p-2 rounded-xl shadow-xl flex items-center gap-2"
      style={{
        left: `${position.x}px`,
        top: `${position.y}px`,
        transform: 'translateX(-50%)',
      }}
    >
      {/* Кнопки форматування (за бажанням можна додати власні події) */}
      <button
        onMouseDown={(e) => {
          e.preventDefault();
          editor.chain().focus().toggleBold().run();
        }}
        className={`p-1.5 rounded-lg transition-all ${
          editor.isActive('bold') ? 'bg-[#4A0E0E] text-white' : 'text-[#4A0E0E] hover:bg-[#F3EAD3]'
        }`}
      >
        <b>B</b>
      </button>

      <button
        onMouseDown={(e) => {
          e.preventDefault();
          editor.chain().focus().toggleItalic().run();
        }}
        className={`p-1.5 rounded-lg transition-all ${
          editor.isActive('italic') ? 'bg-[#4A0E0E] text-white' : 'text-[#4A0E0E] hover:bg-[#F3EAD3]'
        }`}
      >
        <i>I</i>
      </button>

      <button
        onMouseDown={(e) => {
          e.preventDefault();
          editor.chain().focus().toggleUnderline().run();
        }}
        className={`p-1.5 rounded-lg transition-all ${
          editor.isActive('underline') ? 'bg-[#4A0E0E] text-white' : 'text-[#4A0E0E] hover:bg-[#F3EAD3]'
        }`}
      >
        <u>U</u>
      </button>
    </div>
  );
};

export default CustomBubbleMenu;