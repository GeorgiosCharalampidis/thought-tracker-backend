import '@testing-library/jest-dom';
import React from 'react';
import { render, screen } from '@testing-library/react';
import SimilarThoughtsSection from './SimilarThoughtsSection';

describe('SimilarThoughtsSection', () => {
  it('renders separate sections for own related thoughts and community echoes', () => {
    render(
      <SimilarThoughtsSection
        notes={[
          {
            id: 2,
            content: 'Someone else shared something similar.',
            date: '2026-03-12',
            category: 'Anxiety & Overthinking',
            subCategory: 'Overthinking',
          },
        ]}
        ownNotes={[
          {
            id: 1,
            content: 'I have been here before.',
            date: '2026-03-10',
            category: 'Anxiety & Overthinking',
            subCategory: 'Overthinking',
          },
        ]}
        categoryMessage="You're not alone"
        isDarkMode={false}
        onShareAnotherThought={() => {}}
      />,
    );

    const headline = screen.getByText("You're not alone");
    const communityThought = screen.getByText('Someone else shared something similar.');
    const ownHeading = screen.getByText('Your related thoughts');
    const ownThought = screen.getByText('I have been here before.');

    expect(headline).toBeInTheDocument();
    expect(communityThought).toBeInTheDocument();
    expect(screen.getByText('Your related thoughts')).toBeInTheDocument();
    expect(ownThought).toBeInTheDocument();
    expect(screen.queryByText('Community echoes')).not.toBeInTheDocument();
    expect(screen.queryByText('A few of your earlier thoughts echo this too.')).not.toBeInTheDocument();
    expect(screen.queryByText('These are your past entries that feel closest to this moment.')).not.toBeInTheDocument();

    expect(communityThought.compareDocumentPosition(ownHeading) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
    expect(ownHeading.compareDocumentPosition(ownThought) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
  });
});


