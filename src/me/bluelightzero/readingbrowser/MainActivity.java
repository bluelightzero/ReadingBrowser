package me.bluelightzero.readingbrowser;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.IOException;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.app.DialogFragment;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Bitmap;

public class MainActivity extends Activity
{

	WebView webView;

	Bookmark[] bookmarks = new Bookmark[0];
	Bookmark currentBookmark;
	
	boolean isLink = true;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        webView = new WebView(this);
        //webView.loadUrl("http://www.google.co.uk");
        WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webView.setWebViewClient(new ReaderClient());
        setContentView(webView);
		setTitle("Reading Browser - Choose a bookmark");	
        loadBookmarks();
        openBookMarks();
        
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
    	super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(currentBookmark!=null)
			if ((keyCode == KeyEvent.KEYCODE_BACK) && currentBookmark.canGoBack())
			{
				loadUrl(currentBookmark.back());
				return true;
			}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menu_back:
				if(currentBookmark!=null)
					loadUrl(currentBookmark.back());
				return true;
			case R.id.menu_forward:
				if(currentBookmark!=null)
					loadUrl(currentBookmark.forward());
				return true;
			case R.id.menu_refresh:
				if(currentBookmark!=null)
					loadUrl(currentBookmark.getPage());
				return true;
			case R.id.menu_bookmarks:
				openBookMarks();
				return true;
			case R.id.menu_history:
				openHistory();
				return true;
			case R.id.menu_edit:
				editBookmark();
				return true;
			case R.id.menu_delete:
				confirmDelete();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPause()
	{
		saveBookmarks();
		super.onPause();
	}

	public void loadUrl(String url)
	{
		webView.stopLoading();
		isLink = false;
		webView.loadUrl(url);
	}

	public void openBookMarks()
	{
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
		builderSingle.setTitle("Bookmarks");
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,
		android.R.layout.select_dialog_item);
		arrayAdapter.add(" - New - ");
		for(int i=0;i<bookmarks.length;i++)
		{
			if(currentBookmark==bookmarks[i])
			{
				arrayAdapter.add("> "+bookmarks[i].name);
			}
			else
			{
				arrayAdapter.add(bookmarks[i].name);
			}
		}
		builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				//String strName = arrayAdapter.getItem(which);
				if(which==0)
				{
					createBookmark();
				}
				else
				{
					int id=which-1;
					chooseBookmark(id);
				}
			}
		});
		builderSingle.show();
	}

		
	public void openHistory()
	{
		if(currentBookmark!=null)
		{
			AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
			builderSingle.setTitle("History");
			final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,
			android.R.layout.select_dialog_item);
			for(int i=currentBookmark.start;i<=currentBookmark.end;i++)
			{
				if(i==currentBookmark.current)
				{
					arrayAdapter.add("> "+currentBookmark.history[i%currentBookmark.history.length]);
				}
				else
				{
					arrayAdapter.add(currentBookmark.history[i%currentBookmark.history.length]);
				}
			}
			builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					currentBookmark.current = currentBookmark.start+which;
					loadUrl(currentBookmark.getPage());
				}
			});
			builderSingle.show();
		}
	}
		
	public void createBookmark()
	{
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
		builderSingle.setTitle("Create Bookmark");
		builderSingle.setMessage("Name:");
		final EditText input = new EditText(this); 
		builderSingle.setView(input);

		builderSingle.setPositiveButton("New Page", new DialogInterface.OnClickListener()
		{  
			public void onClick(DialogInterface dialog, int whichButton)
			{  
				String value = input.getText().toString();
				Bookmark b = new Bookmark();
				b.name = value;
				b.history[0] = "http://www.google.co.uk";
				addBookmark(b);
				chooseBookmark(bookmarks.length-1);
				return;                  
			}  
		});  

		if(currentBookmark!=null)
			builderSingle.setNegativeButton("This Page", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
				String value = input.getText().toString();
				if(currentBookmark!=null)
				{
					Bookmark b = new Bookmark();
					b.name = value;
					b.history[0] = currentBookmark.getPage();
					addBookmark(b);
					chooseBookmark(bookmarks.length-1);
				}
				return;   
				}
			});
			
		builderSingle.show();
	}
    
	public void confirmDelete()
	{
		if(currentBookmark!=null)
		{
			AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
			builderSingle.setTitle("Delete");
			builderSingle.setMessage("Are you sure you want to delete this bookmark?");
			//final EditText input = new EditText(this); 
			//builderSingle.setView(input);

			builderSingle.setPositiveButton("Yes", new DialogInterface.OnClickListener()
			{  
				public void onClick(DialogInterface dialog, int whichButton)
				{  
					deleteBookmark(currentBookmark);
					currentBookmark = null;
					loadUrl("about:blank");
					setTitle("Reading Browser - Choose a bookmark");	
					openBookMarks();
					return;                  
				}  
			});  

			if(currentBookmark!=null)
				builderSingle.setNegativeButton("No", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						return;   
					}
				});

			builderSingle.show();
		}
	}

  
	public void editBookmark()
	{
		if(currentBookmark!=null)
		{
			AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
			builderSingle.setTitle("Edit Bookmark");
			builderSingle.setMessage("Name:");
			final EditText input = new EditText(this); 
			input.setText(currentBookmark.name, TextView.BufferType.EDITABLE);
			input.selectAll();
			builderSingle.setView(input);

			builderSingle.setPositiveButton("Save", new DialogInterface.OnClickListener()
			{  
				public void onClick(DialogInterface dialog, int whichButton)
				{  
					String value = input.getText().toString();
					currentBookmark.name = value;
					setTitle("Reading Browser - "+currentBookmark.name);	
					return;                  
				}  
			});  

			builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which)
				{
					return;   
				}
			});

			builderSingle.show();
		}
	}
  
    
	private void loadBookmarks()
	{
		String eol = System.getProperty("line.separator");
		BufferedReader input = null;
		try
		{
			input = new BufferedReader(new InputStreamReader(openFileInput("Bookmarks")));
			String line;
			bookmarks = new Bookmark[0];
			System.out.println("Load:");
			while ((line = input.readLine()) != null)
			{
				System.out.println("  "+line);
				String[] split = line.split("\\|");
				Bookmark b = new Bookmark();
				b.name = split[0];
				b.history[0] = split[2];
				for(int i=3;i<split.length;i++)
				{
					System.out.println("    "+split[i]);
					b.add(split[i]);
				}
				b.current = Integer.parseInt(split[1]);
				addBookmark(b);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (input != null)
			{
				try
				{
					input.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	} 
    
	private void saveBookmarks()
	{
		String eol = System.getProperty("line.separator");
		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new OutputStreamWriter(openFileOutput("Bookmarks", MODE_WORLD_WRITEABLE)));
			System.out.println("Save:");
			for(int i=0;i<bookmarks.length;i++)
			{
				System.out.println("  "+bookmarks[i].toString());
				writer.write(bookmarks[i].toString() + eol);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (writer != null)
			{
				try
				{
					writer.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
		
	public void addBookmark(Bookmark b)
	{
		Bookmark[] newBookmarks = new Bookmark[bookmarks.length+1];
		for(int i=0;i<bookmarks.length;i++)
		{
			newBookmarks[i] = bookmarks[i];
		}
		newBookmarks[bookmarks.length] = b;
		bookmarks = newBookmarks;
	}
		
	public void deleteBookmark(Bookmark b)
	{
		Bookmark[] newBookmarks = new Bookmark[bookmarks.length-1];
		int c = 0;
		for(int i=0;i<bookmarks.length;i++)
		{
			if(bookmarks[i]!=b)
			{
				newBookmarks[c] = bookmarks[i];
				c++;
			}
		}
		bookmarks = newBookmarks;
	}
		
	public void chooseBookmark(int id)
	{
		currentBookmark = bookmarks[id];
		setTitle("Reading Browser - "+currentBookmark.name);	
		
    	loadUrl(currentBookmark.getPage());
	}
	
	class ReaderClient extends WebViewClient
	{
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			isLink = true;
			return false;
		}

		@Override
		public void onPageFinished(WebView view, String url)
		{
			if(currentBookmark!=null)
			{
				String current = currentBookmark.getPage();
				if(isLink)
				{
					currentBookmark.add(url);
				}
			}
		}
	}
	
		
	class Bookmark
	{
		public String name = "";
		public String[] history = new String[10];
		public int start = 0;
		public int end = 0;
		public int current = 0;

		public void add(String url)
		{
			current++;
			end = current;
			history[current%history.length] = url;
			if(current%history.length==start%history.length)
			{
				start++;
			}
		}

		public boolean canGoBack()
		{
			return current>start;
		}

		public String getPage()
		{
			return history[current%history.length];	
		}

		public String back()
		{
			if(current>start)
			{
				current--;
			}
			return history[current%history.length];
		}

		public boolean canGoForward()
		{
			return current<end;
		}

		public String forward()
		{
			if(current<end)
			{
				current++;
			}
			return history[current%history.length];
		}

		public String toString()
		{
			String s = "";
			s+=name+"|"+(current-start);
			for(int i=start;i<=end;i++)
			{
				s+="|"+history[i%history.length];
			}
			return s;
		}
	}
}
