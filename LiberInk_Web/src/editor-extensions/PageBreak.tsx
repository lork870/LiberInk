import { Node, mergeAttributes } from '@tiptap/core';

export const PageBreak = Node.create({
  name: 'pageBreak',
  group: 'block',
  atom: true,

  parseHTML() {
    return [{ tag: 'div[data-type="page-break"]' }];
  },

  renderHTML({ HTMLAttributes }) {
    return [
      'div', 
      mergeAttributes(HTMLAttributes, { 
        'data-type': 'page-break',
        class: 'page-break',
        style: 'height: 40px; margin: 20px 0; border-top: 2px dashed #E8E2D2; pointer-events: none;'
      })
    ];
  },

  addCommands() {
    return {
      setPageBreak: () => ({ commands }: { commands: any }) => {
        return commands.insertContent({ type: this.name });
      },
    } as any;
  },
});