export default function(spec) {
  spec.describe('Listing the employees', () => {
    spec.it('filters the list by search input', async () => {
      await spec.exists('EmployeeListItem.AnupGuptax');
      await spec.fillIn('SearchBar.TextInput', 'Amy');
      await spec.exists('EmployeeListItem.AmyTaylor');
    });
  });

  spec.describe('Tapping on an employee', () => {
    spec.it('shows a button to email them', async () => {
      await spec.fillIn('SearchBar.TextInput', 'Amy');
      await spec.press('EmployeeListItem.AmyTaylor');
      await spec.pause(1000);
      await spec.exists('ActionBar.EmailButton');
    });
  });
}
