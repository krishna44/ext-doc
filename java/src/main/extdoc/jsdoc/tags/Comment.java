package extdoc.jsdoc.tags;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Andrey Zubkov
 * Date: 30.10.2008
 * Time: 21:41:31
 */

public class Comment {

    private final List<Tag> tagList = new ArrayList<Tag>();

    private String description;

    public String getDescription() {
        return description;
    }

    public Tag[] tags(){
        return tagList.toArray(new Tag[tagList.size()]);
    }

    public Tag[] tags(String tagName){
        List<Tag> found = new ArrayList<Tag>();
        for(Tag tag : tagList){
            if (tag.name().equals(tagName)){
                found.add(tag);
            }
        }
        return found.toArray(new Tag[found.size()]);
    }

    private enum CommentState {SPACE, DESCRIPTION}
    private enum InnerState {TAG_NAME, TAG_GAP, IN_TEXT}

    /**
     * Constructor of Comment
     * @param content Comment
     */
    public Comment(final String content){

        class CommentStringParser{

            private boolean isStarWhite(char ch){
                  return Character.isWhitespace(ch) || ch=='*';
            }            

            /**
             * Processes inner comment text
             * Very similar to Sun's com.sun.tools.javadoc#Comment             
             */
            void parseCommentStateMacine(){
                    CommentState state = CommentState.SPACE;
                    StringBuilder buffer = new StringBuilder();
                    StringBuilder spaceBuffer = new StringBuilder();
                    boolean foundStar = false;
                    for (int i=0;i<content.length();i++){
                        char ch = content.charAt(i);
                        switch (state){
                            case SPACE:
                                if (isStarWhite(ch)){
                                    if (ch == '*'){
                                        foundStar = true;
                                    }
                                    spaceBuffer.append(ch);
                                    break;
                                }
                                if (!foundStar){
                                    buffer.append(spaceBuffer);
                                }
                                spaceBuffer.setLength(0);
                                state = CommentState.DESCRIPTION;
                                /* fall through */
                            case DESCRIPTION:
                                if (ch == '\n'){
                                    foundStar = false;
                                    state = CommentState.SPACE;
                                }
                                buffer.append(ch);
                                break;
                        }
                    }

                    InnerState instate = InnerState.TAG_GAP;
                    String inner = buffer.toString();

                    String tagName = null;
                    int tagStart =0;
                    int textStart =0;
                    boolean newLine = true;
                    int lastNonWhite = -1;
                    int len = inner.length();
                    for(int i=0;i<len;i++){
                        char ch = inner.charAt(i);
                        boolean isWhite = Character.isWhitespace(ch);
                        switch (instate){
                            case TAG_NAME:
                                if (isWhite){
                                    tagName = inner.substring(tagStart, i);
                                    instate = InnerState.TAG_GAP;
                                }
                                break;
                            case TAG_GAP:
                                if (isWhite){
                                    break;
                                }
                                textStart = i;
                                instate = InnerState.IN_TEXT;
                                /* fall through */
                            case IN_TEXT:
                                if (newLine && ch == '@'){
                                    parseCommentComponent(inner, tagName,
                                            textStart,lastNonWhite+1);
                                    tagStart = i;
                                    instate = InnerState.TAG_NAME;
                                }
                                break;
                        }
                        if (ch == '\n'){
                            newLine = true;
                        }else if(!isWhite){
                            lastNonWhite = i;
                            newLine = false;
                        }
                    }
                    // Finish for last item
                    switch(instate){
                        case TAG_NAME:
                            tagName = inner.substring(tagStart, len);
                            /* fall through */
                        case TAG_GAP:
                            textStart = len;
                        case IN_TEXT:
                            parseCommentComponent(inner, tagName, textStart,
                                    lastNonWhite+1);
                            break;
                    }
            }

             private void parseCommentComponent(String content,
                                                    String tagName, int from, int upto) {
                String tx = upto <= from ? "": content.substring(from, upto);
                if (tagName == null){
                    description = tx;
                }else{
                    TagImpl tag;
                    if (tagName.equals("@class")){
                        tag = new ClassTagImpl(tagName, tx);
                    }else{
                        tag = new TagImpl(tagName, tx);
                    }
                    tagList.add(tag);
                }
            }
            
        }
        new CommentStringParser().parseCommentStateMacine();

    }

}
