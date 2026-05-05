import { Mark, mergeAttributes } from '@tiptap/core';

export interface NoteOptions {
  HTMLAttributes: Record<string, any>;
}

declare module '@tiptap/core' {
  interface Commands<ReturnType> {
    note: {
      setNote: (attributes: { id: string; type: 'Author' | 'Quick' }) => ReturnType;
      unsetNote: () => ReturnType;
    };
  }
}

export const NoteMark = Mark.create<NoteOptions>({
  name: 'note',

  addOptions() {
    return { HTMLAttributes: {} };
  },

  addAttributes() {
    return {
      id: {
        default: null,
        parseHTML: element => element.getAttribute('data-note-id'),
        renderHTML: attributes => ({ 'data-note-id': attributes.id }),
      },
      type: {
        default: 'Author',
        parseHTML: element => element.getAttribute('data-note-type'),
        renderHTML: attributes => {
          const isAuthor = attributes.type === 'Author';
          return {
            'data-note-type': attributes.type,
            class: `liberink-note cursor-pointer border-b-2 border-dashed transition-colors ${
              isAuthor 
                ? 'bg-green-200/40 border-green-500 text-green-900' 
                : 'bg-purple-200/40 border-purple-500 text-purple-900'
            }`,
          };
        },
      },
    };
  },

  parseHTML() {
    return [{ tag: 'span[data-note-id]' }];
  },

  renderHTML({ HTMLAttributes }) {
    return ['span', mergeAttributes(this.options.HTMLAttributes, HTMLAttributes), 0];
  },

  addCommands() {
    return {
      setNote: attributes => ({ commands }) => commands.setMark(this.name, attributes),
      unsetNote: () => ({ commands }) => commands.unsetMark(this.name),
    };
  },
});